package com.example.covid_track_2021.ui.qrCodeScanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.covid_track_2021.R;
import com.example.covid_track_2021.database.LocationsVisitedTracking.LocationsTrackingService;
import com.example.covid_track_2021.databinding.FragmentScannerBinding;
import com.example.covid_track_2021.qrCode.LocationElapsedTime;
import com.example.covid_track_2021.qrCode.QRCodeHandler;
import com.example.covid_track_2021.qrCode.QRCodeImageAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ScannerFragment extends Fragment {

    private ScannerViewModel scannerViewModel;
    private FragmentScannerBinding binding;
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private LocationsTrackingService locationsVisitedService = null;

    private Button qrCodeFoundButton;
    private String qrCode;
    private HashMap<String, LocationElapsedTime> mapOfLocationElapsedTime;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        scannerViewModel =
                new ViewModelProvider(this).get(ScannerViewModel.class);

        binding = FragmentScannerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        previewView = root.findViewById(R.id.activity_main_previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.getContext());
        qrCodeFoundButton = root.findViewById(R.id.activity_main_qrCodeFoundButton);
        qrCodeFoundButton.setVisibility(View.INVISIBLE);
        qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("TEST QR");
            }
        });

        locationsVisitedService = new LocationsTrackingService(this.getContext());

        mapOfLocationElapsedTime = new HashMap<>();

        requestCamera();

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                System.out.println("Camera Permission Denied");
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("Error starting camera " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this.getContext()));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this.getContext()), new QRCodeImageAnalyzer(new QRCodeHandler() {
            @Override
            public void onQRCodeFound(String qrCodeHash) {
                qrCode = qrCodeHash;
                //qrCodeFoundButton.setVisibility(View.VISIBLE);

                LocationElapsedTime locationElapsedTime = mapOfLocationElapsedTime.get(qrCode);

                // if user not found add it
                if(locationElapsedTime == null){
                    LocationElapsedTime newLocationElapsedTime = new LocationElapsedTime();
                    newLocationElapsedTime.setQRCode(qrCode);
                    newLocationElapsedTime.setStartTime(System.nanoTime());

                    mapOfLocationElapsedTime.put(qrCode, newLocationElapsedTime);
                    return;
                }else if (Math.abs(locationElapsedTime.getStartTime() - System.nanoTime()) / 60000 < 5 ){ // If exists but was added not long ago return
                    System.out.println("Location already registered");
                    return;
                }

                // Add location into DB
                locationsVisitedService.locationFound(qrCodeHash);
            }

            @Override
            public void qrCodeNotFound() {
                qrCodeFoundButton.setVisibility(View.INVISIBLE);
            }
        }));

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
}