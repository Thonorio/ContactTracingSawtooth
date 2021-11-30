package com.example.covid_track_2021.blockchainAPI;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import sawtooth.sdk.protobuf.Batch;
import sawtooth.sdk.protobuf.BatchHeader;
import sawtooth.sdk.protobuf.BatchList;
import sawtooth.sdk.protobuf.Transaction;
import sawtooth.sdk.protobuf.TransactionHeader;
import sawtooth.sdk.signing.PrivateKey;
import sawtooth.sdk.signing.PublicKey;
import sawtooth.sdk.signing.Signer;


public class BlockchainController implements BlockchainHandler {

    // Transaction Variables
    private Signer signer;
    private String address;
    private PublicKey publicKey;
    private final static String familyVersion = "1.0";
    private final static String transactionFamilyName = "covidTracker";

    // Rest Variables
    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("application/octet-stream");

    public BlockchainController() {
    }

    public BlockchainController(PrivateKey privateKey)  {
        signer = new Signer(context, privateKey);
        publicKey = signer.getPublicKey();

        address = hashDigestByteArrayOutputStream(transactionFamilyName.getBytes()).substring(0, 6) + hashDigestByteArrayOutputStream(publicKey.hex().getBytes()).substring(0, 64);
        System.out.println("Address " + address);
    }

    @Override
    public void wrapAndSend(String baseUrl,List<ByteArrayOutputStream> listOfPayloads){

        List<Transaction> transactions = new ArrayList();
        byte[] payloadBytes;

        for (ByteArrayOutputStream payload:listOfPayloads) {
            payloadBytes = payload.toByteArray();

            // Create Transaction Header
            TransactionHeader header = getTransactionHeader(address, payload);

            // Create Transaction
            Transaction transaction = getTransaction(payloadBytes, header);

            // Add transaction to list of transactions
            transactions.add(transaction);
        }

        List<String> collect = transactions.stream()
                                    .map(Transaction::getHeaderSignature)
                                    .collect(Collectors.toList());

        // Create Batch Header
        BatchHeader batchHeader = getBatchHeader(collect);

        // Create Batch
        Batch batch = getBatch(transactions, batchHeader);

        ByteString batchListBytes = BatchList.newBuilder()
                                        .addBatches(batch)
                                        .build()
                                        .toByteString();

        submitTransaction(baseUrl, batchListBytes);
    }

    @Override
    public void getState(String baseUrl) {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "state/" + address)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = client.newCall(request).execute().body()) {
                        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        System.out.println(responseBody.string());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Batch getBatch(List<Transaction> transactions, BatchHeader batchHeader) {
        return Batch.newBuilder()
                    .setHeader(batchHeader.toByteString())
                    .addAllTransactions(transactions)
                    .setHeaderSignature(signer.sign(batchHeader.toByteArray()))
                    .build();
    }

    @Override
    public BatchHeader getBatchHeader(List<String> collect) {
        return BatchHeader.newBuilder()
                    .setSignerPublicKey(publicKey.hex())
                    .addAllTransactionIds(collect) // transaction.getHeaderSignature()
                    .build();
    }

    @Override
    public Transaction getTransaction(byte[] payloadBytes, TransactionHeader header) {
        return Transaction.newBuilder()
                    .setHeader(header.toByteString())
                    .setPayload(ByteString.copyFrom(payloadBytes))
                    .setHeaderSignature(signer.sign(header.toByteArray()))
                    .build();
    }

    @Override
    public TransactionHeader getTransactionHeader(String address, ByteArrayOutputStream payload) {
        return TransactionHeader.newBuilder()
                    .setSignerPublicKey(publicKey.hex())
                    .setFamilyName(transactionFamilyName)
                    .setFamilyVersion(familyVersion)
                    .addInputs(address)
                    .addOutputs(address)
                    .setPayloadSha512(hashDigestByteArrayOutputStream(payload))
                    .setBatcherPublicKey(publicKey.hex())
                    .setNonce(UUID.randomUUID().toString())
                    .build();
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Future<String> getStateInformation(String baseUrl, String blockId){
        String url = baseUrl + "state?address=" + blockId;

        System.out.println("Address / BLock ID " + blockId);
        CompletableFuture<String> future = new CompletableFuture<>();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    future.complete(response.body().string());
                }
            }
        });
        return future;
    }

    private void submitTransaction(String baseUrl, ByteString batchListBytes) {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "batches")
                    .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, batchListBytes.toByteArray()))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        System.out.println(responseBody.string());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String hashDigestByteArrayOutputStream(ByteArrayOutputStream input) {
        return hashDigestByteArrayOutputStream(input.toByteArray());
    }

    public String hashDigestByteArrayOutputStream(byte[] input){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return BaseEncoding.base16().lowerCase().encode(digest.digest());
    }

}
