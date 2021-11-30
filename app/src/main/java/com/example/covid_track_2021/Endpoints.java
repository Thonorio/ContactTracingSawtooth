package com.example.covid_track_2021;

public enum Endpoints {
    URL("http://192.168.1.168:8008/"),
    SOCKET_ADDRESS("tcp://192.168.1.168:4004");

    public final String value;

    private Endpoints(String value) {
        this.value = value;
    }
}
