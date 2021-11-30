package com.example.covid_track_2021.qrCode;

public class LocationElapsedTime {
    private String QRCode;

    long startTime = System.currentTimeMillis();

    public String getQRCode() {
        return QRCode;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
