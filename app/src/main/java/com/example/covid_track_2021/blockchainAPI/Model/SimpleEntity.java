package com.example.covid_track_2021.blockchainAPI.Model;

public class SimpleEntity {
    protected String data;
    protected String head;
    protected String link;

    public SimpleEntity() {
    }

    public SimpleEntity(String data, String head, String link) {
        this.data = data;
        this.head = head;
        this.link = link;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
