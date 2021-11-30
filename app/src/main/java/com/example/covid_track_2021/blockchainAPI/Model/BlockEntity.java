package com.example.covid_track_2021.blockchainAPI.Model;

import java.util.List;

public class BlockEntity {
    private List<DataEntity> data;
    private String head;
    private String link;
    private PagingEntity paging;

    public BlockEntity() {
    }

    public BlockEntity(List<DataEntity> data, String head, String link, PagingEntity paging) {
        this.data = data;
        this.head = head;
        this.link = link;
        this.paging = paging;
    }

    public List<DataEntity> getData() {
        return data;
    }

    public void setData(List<DataEntity> data) {
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

    public PagingEntity getPaging() {
        return paging;
    }

    public void setPaging(PagingEntity paging) {
        this.paging = paging;
    }
}
