package com.example.covid_track_2021.ui.qrCodeScanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScannerViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ScannerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}