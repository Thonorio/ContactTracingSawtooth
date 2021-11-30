package com.example.covid_track_2021.beacon;

import java.util.UUID;

public class PatientElapsedTime {
    private UUID uuid;

    long startTime = System.currentTimeMillis();
    long estimatedTime = System.currentTimeMillis() - startTime;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
}
