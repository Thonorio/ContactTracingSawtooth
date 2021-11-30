package com.example.covid_track_2021.blockchainAPI.Model;

public class PagingEntity {
    private String limit;
    private String start;

    public PagingEntity() {
    }

    public PagingEntity(String limit, String start) {
        this.limit = limit;
        this.start = start;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }
}
