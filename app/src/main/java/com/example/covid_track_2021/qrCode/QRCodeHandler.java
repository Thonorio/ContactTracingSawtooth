package com.example.covid_track_2021.qrCode;

public interface QRCodeHandler {
    void onQRCodeFound(String qrCode);
    void qrCodeNotFound();
}