package com.example.covid_track_2021.beacon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.example.covid_track_2021.database.ContactedUsers.ContactedUsersService;
import com.example.covid_track_2021.notificações.NotificationService;
import com.example.covid_track_2021.uniqueIdentifier.UUIDManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class BeaconService extends Service implements BeaconConsumer, BootstrapNotifier, RangeNotifier {

    //Services
    private NotificationService notificationService = null;
    private ContactedUsersService contactedUsersService = null;

    // Beacon
    private BeaconManager beaconManager;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private HashMap<UUID, PatientElapsedTime> mapOfPatientElapsedTime;


    @Override
    public void onCreate() {
        super.onCreate();
        mapOfPatientElapsedTime = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Initialize Notification Manager
        notificationService = new NotificationService(getSystemService(NotificationManager.class));

        // Create Notification
        Notification notification = notificationService.createNotification(this, "Exposure Tracker is running in the background");

        // Create Notification
        // Notification notification = notificationService.createNotification(this, "Contacted with an infected user. " +
        //        "\nPlease visit a testing center");

        // Start Foreground Notification
        startForeground(1, notification);

        // Get Database Instance
        contactedUsersService = new ContactedUsersService(this);

        // Start Beacon Tracker
        setupBeaconTracker();

        // Start Beacon Advertisement
        startBeaconAdvertisement();

        //stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupBeaconTracker() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // In this example, we will use Eddystone protocol, so we have to define it here
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        beaconManager.bind(this);

        beaconManager.setBackgroundBetweenScanPeriod(10000);

        //beaconManager.setDebug(true);
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    private void startBeaconAdvertisement() {
        // This code block starts beacon transmission
        Beacon beacon = new Beacon.Builder()
                .setId1(UUIDManager.getInstance().getUuid().toString())
                .setId2("")
                .setId3("")
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(new ArrayList<Long>(Arrays.asList(1234567L))) // Remove this for beacon layouts without d: fields
                .build();

        try{
            // Change the layout below for other beacon types
            BeaconParser beaconParser = new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT);
            BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
            beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
                @Override
                public void onStartFailure(int errorCode) {
                    System.out.println("Advertisement start failed with code: "+errorCode);
                }

                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    System.out.println("Advertisement start succeeded.");
                }
            });
        }catch (Exception e){
            System.out.println("EXCEPTION " + e);
        }

    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
    }

    @Override
    public void didEnterRegion(Region arg0) {
        regionBootstrap.disable();
    }

    @Override
    public void didExitRegion(Region region) {
        System.out.println("I no longer see a beacon.");
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        System.out.println("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon : beacons) {
                    if(beacon.getDistance() < 2.5){
                        String userUUID = String.valueOf(beacon.getId1());
                        PatientElapsedTime patientElapsedTime = mapOfPatientElapsedTime.get(userUUID);

                        // if user not found add it
                        if(patientElapsedTime == null){
                            PatientElapsedTime newPatientElapsedTime = new PatientElapsedTime();
                            patientElapsedTime.setUuid(UUID.fromString(userUUID));
                            patientElapsedTime.setStartTime(System.nanoTime());
                            patientElapsedTime.setEstimatedTime(System.nanoTime());

                            mapOfPatientElapsedTime.put(UUID.fromString(userUUID), newPatientElapsedTime);
                            return;
                        }


                        if (patientElapsedTime != null){
                            // if found but not recently update it
                            if( (Math.abs(System.nanoTime() - patientElapsedTime.getEstimatedTime()) / 60000) > 5){
                                patientElapsedTime.setEstimatedTime(System.nanoTime());
                                patientElapsedTime.setEstimatedTime(System.nanoTime());
                                return;
                            }else{ // if found in the last 5 minutes update end time
                                patientElapsedTime.setEstimatedTime(System.nanoTime());
                            }
                        }

                        // if time elapsed if bigger than 5 minutes add to database
                        if(Math.abs(patientElapsedTime.getEstimatedTime() - patientElapsedTime.getStartTime()) / 60000 > 5 ){
                            //notificationService.setMessage("App is working on background");
                            //notificationService.sendNotification(ForegroundService.this);
                            contactedUsersService.contactFound(userUUID);
                            System.out.println("The first beacon I see is about "+ beacon.getDistance()+" meters away.");
                        }
                    }
                   // Log.i(TAG, "The first beacon I see is about "+ beacon.getDistance()+" meters away.");
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("backgroundRegion", null, null, null));
        } catch (RemoteException e) {    }
    }
}
