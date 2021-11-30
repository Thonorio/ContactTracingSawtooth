package com.example.covid_track_2021.blockchainAPI.Model;

public class DataEntity {

    private String address;

    private String data;

    public DataEntity() {
    }

    public DataEntity(String address, String data) {
        this.address = address;
        this.data = data;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
