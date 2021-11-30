package com.example.covid_track_2021.subscriber;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.covid_track_2021.Endpoints;
import com.example.covid_track_2021.blockchainAPI.BlockchainController;
import com.example.covid_track_2021.blockchainAPI.Model.BlockEntity;
import com.example.covid_track_2021.blockchainAPI.Model.DataEntity;
import com.example.covid_track_2021.database.ContactedUsers.ContactedUsersService;
import com.example.covid_track_2021.notificações.NotificationService;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;

import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import sawtooth.sdk.messaging.Future;
import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.processor.exceptions.ValidatorConnectionError;
import sawtooth.sdk.protobuf.ClientEventsSubscribeRequest;
import sawtooth.sdk.protobuf.ClientEventsSubscribeResponse;
import sawtooth.sdk.protobuf.EventFilter;
import sawtooth.sdk.protobuf.EventList;
import sawtooth.sdk.protobuf.EventSubscription;
import sawtooth.sdk.protobuf.Message;

public class SubscriberService extends Service {

    private Stream eventStream;
    private Boolean working = true;
    private static final String baseURL = "http://192.168.1.168:8008/";
    private ContactedUsersService contactedUsersService;
    private NotificationService notificationService = null;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                eventStream = new Stream(Endpoints.SOCKET_ADDRESS.value);
                getUpdatedIP();
            }catch (Exception e){
                Log.i("ERROR -> ", e.toString() );
            }
           startSubscription(getApplicationContext());
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    public SubscriberService() {
    }

    @Override
    public void onCreate() {
        // start new thread and you your work there
        new Thread(runnable).start();
    }

    @Override
    public void onDestroy() {
        working = false;
    }


    public void startSubscription(Context context) {
        this.contactedUsersService = new ContactedUsersService(context);

        Gson jsonParser = new Gson();
        BlockchainController requestHandler = new BlockchainController();
        EventSubscription mySubscription = buildStateDeltaEvent(setupEventFilter());
        ClientEventsSubscribeRequest subsReq = buildEventRequest(buildBlockCommitEvent(), mySubscription);
        Future sawtoothSubsFuture = wrapMessage(subsReq);

        try {
            Log.i("LOG:", "STARTED LISTENING");

            ClientEventsSubscribeResponse eventSubsResp = sendEventRequest(sawtoothSubsFuture);

            if (eventSubsResp.getStatus().equals(ClientEventsSubscribeResponse.Status.UNKNOWN_BLOCK)) {
                System.out.println("Unknown block ");
            }

            if (!eventSubsResp.getStatus().equals(ClientEventsSubscribeResponse.Status.OK)) {
                System.out.println("Subscription failed with status " + eventSubsResp.getStatus());
                throw new RuntimeException("cannot connect ");
            }
            listenForChanges(working, jsonParser, requestHandler);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ValidatorConnectionError validatorConnectionError) {
            validatorConnectionError.printStackTrace();
        }
    }

    private void listenForChanges(Boolean working, Gson jsonParser, BlockchainController requestHandler) throws InvalidProtocolBufferException {
        // Listen for as long as possible
        while (working ) { // && System.currentTimeMillis() < end
            // Fetch all events
            EventList eventList = EventList.parseFrom(eventStream.receive().getContent());
            eventList.getEventsList().forEach(event -> {

                // Filter only the events you need
                if (event.getEventType().equals("sawtooth/state-delta")) {

                    event.getAttributesList().forEach(attribute -> {

                        // Get the address where changes where made
                        if(attribute.getKey().equals("address")){
                            try {
                                getDataFromAddress(requestHandler, jsonParser, attribute.getValue());
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }

    private void getDataFromAddress(BlockchainController requestHandler, Gson jsonParser, String attribute) throws ExecutionException, InterruptedException {
        java.util.concurrent.Future<String> blockFuture = requestHandler.getStateInformation(baseURL, attribute);
        StringBuilder listOfUUIDs;
        List<DataEntity> dataEntities;
        while (!blockFuture.isDone()){
            listOfUUIDs = new StringBuilder();
            listOfUUIDs.append("(");
            dataEntities = jsonParser.fromJson(blockFuture.get(), BlockEntity.class).getData();

            for (DataEntity data : dataEntities) {

                byte[] byteArrayOfUUIDs = Base64.getDecoder().decode(data.getData());
                for (String uuid : parseUUIDFromByteArray(byteArrayOfUUIDs)) {
                    try{
                        listOfUUIDs.append("'" + UUID.fromString(uuid) + "',");
                    }catch (IllegalArgumentException e){
                        Log.i("LOG -> Not UUID", uuid);
                        continue;
                    };
                }
            }

            int index = listOfUUIDs.length() > 1 ? listOfUUIDs.length() - 1 : listOfUUIDs.length();
            listOfUUIDs.setLength(index);
            listOfUUIDs.append(")");

            if(this.contactedUsersService.wasThereAContactWithinTheLast14Days(listOfUUIDs.toString())){
                this.notificationService = new NotificationService(getSystemService(NotificationManager.class));

                // Create Notification
                Notification notification = this.notificationService.createNotification(this, "Contacted with an infected user. " +
                        "\nPlease visit a testing center");

                // Start Foreground Notification
                startForeground(1, notification);
            }

        }
    }

    private String[] parseUUIDFromByteArray(byte[] byteArrayOfUUIDs) {
        String[] infectedIDs = new String(byteArrayOfUUIDs).split(",");
        for (int i = 0; i < infectedIDs.length; i++) {
            infectedIDs[i] = infectedIDs[i].replaceAll("\\[|\\]", "").replaceAll("\"", "");
        }
        return infectedIDs;
    }

    @NotNull
    private EventFilter setupEventFilter() {
        EventFilter eventFilter = EventFilter.newBuilder().setKey("address")
                .setMatchString(".*")
                .setFilterType(EventFilter.FilterType.REGEX_ANY).build();
        return eventFilter;
    }
    @NotNull
    private EventSubscription buildStateDeltaEvent(EventFilter eventFilter) {
        // Setup a connection to the validator
        EventSubscription mySubscription = EventSubscription.newBuilder().setEventType("sawtooth/state-delta")
                .addFilters(eventFilter)
                .build();
        return mySubscription;
    }

    @NotNull
    private EventSubscription buildBlockCommitEvent() {
        //subscribe to sawtooth/block-commit
        EventSubscription deltaSubscription = EventSubscription.newBuilder().setEventType("sawtooth/block-commit")
                .build();
        return deltaSubscription;
    }

    @NotNull
    private ClientEventsSubscribeRequest buildEventRequest(EventSubscription deltaSubscription, EventSubscription mySubscription) {
        // Construct the request
        ClientEventsSubscribeRequest subsReq = ClientEventsSubscribeRequest.newBuilder()
                .addLastKnownBlockIds("0000000000000000").addSubscriptions(deltaSubscription).addSubscriptions(mySubscription)
                .build();
        return subsReq;
    }

    @NotNull
    private Future wrapMessage(ClientEventsSubscribeRequest subsReq) {
        // Construct the message wrapper
        Future sawtoothSubsFuture = eventStream.send(Message.MessageType.CLIENT_EVENTS_SUBSCRIBE_REQUEST,
                subsReq.toByteString());
        return sawtoothSubsFuture;
    }

    private ClientEventsSubscribeResponse sendEventRequest(Future sawtoothSubsFuture) throws InvalidProtocolBufferException, InterruptedException, ValidatorConnectionError {
        // Send the request
        ClientEventsSubscribeResponse eventSubsResp = ClientEventsSubscribeResponse
                .parseFrom(sawtoothSubsFuture.getResult());
        return eventSubsResp;
    }

    private void getUpdatedIP(){
        Gson gson = new Gson();

        try {
            java.util.concurrent.Future<String> blockFuture = new BlockchainController().getStateInformation(baseURL, "23a2c839afd748f4a02ee18f75150cb845e84500fa2d5975e84d8dd5cbe70c53bcaebb");
            List<DataEntity> dataEntities;
            while (!blockFuture.isDone()){

                Log.i("TEST","TESTING");
                Log.i("TEST", gson.fromJson(blockFuture.get(), BlockEntity.class).getData().toString());
                for (DataEntity data : gson.fromJson(blockFuture.get(), BlockEntity.class).getData()) {
                    Log.i("IPs","Nodes IP" + new String(Base64.getDecoder().decode(data.getData())));
                }
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}