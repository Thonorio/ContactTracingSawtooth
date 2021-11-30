package com.example.covid_track_2021.blockchainAPI;

import java.io.ByteArrayOutputStream;
import java.util.List;

import sawtooth.sdk.protobuf.Batch;
import sawtooth.sdk.protobuf.BatchHeader;
import sawtooth.sdk.protobuf.Transaction;
import sawtooth.sdk.protobuf.TransactionHeader;
import sawtooth.sdk.signing.PrivateKey;
import sawtooth.sdk.signing.PublicKey;
import sawtooth.sdk.signing.Secp256k1Context;
import sawtooth.sdk.signing.Secp256k1PrivateKey;

public interface BlockchainHandler {

    Secp256k1Context context = new Secp256k1Context();

    static PrivateKey generatePrivateKey() {
        return context.newRandomPrivateKey();
    }

    static PrivateKey getPrivateKeyFromString(byte[] privateKey) {
        return new Secp256k1PrivateKey(privateKey);
    }

    void wrapAndSend(String baseUrl, List<ByteArrayOutputStream> listOfPayloads);

    void getState(String baseUrl);

    Batch getBatch(List<Transaction> transactions, BatchHeader batchHeader);

    BatchHeader getBatchHeader(List<String> collect);

    Transaction getTransaction(byte[] payloadBytes, TransactionHeader header);

    TransactionHeader getTransactionHeader(String address, ByteArrayOutputStream payload) ;

    PublicKey getPublicKey();
}
