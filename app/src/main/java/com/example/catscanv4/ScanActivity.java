package com.example.catscanv4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;


import com.example.catscanv4.databinding.ActivityScanBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity implements Detector.DetectorListener, Detector.OnModelSetupListener {

    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private Boolean isfrontCamera = false;

    private ProcessCameraProvider cameraProvider;
    private Bitmap reusableBitmap;

    private ExecutorService cameraExecutor;

    private ActivityScanBinding binding;

    Detector detector;
    private OverlayView overlay;

    Button upload, home, logs;

    private Boolean isCameraInitialized = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        detector = new Detector(getBaseContext(), Constants.MODEL_PATH,Constants.LABELS_PATH,this,this);
        detector.SetupAsync();

        overlay = findViewById(R.id.overlay);

        cameraExecutor = Executors.newSingleThreadExecutor();

        upload = findViewById(R.id.btnUpload);
        home = findViewById(R.id.btnHome);
        logs = findViewById(R.id.btnLogs);

        cameraStart();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        logs.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this, LogsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void cameraStart(){
        if (cameraProvider != null){
            bindCameraUses();
        }else{
            ListenableFuture <ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
            cameraProviderFuture.addListener(() ->{
                try{
                    cameraProvider = cameraProviderFuture.get();
                    bindCameraUses();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }, ContextCompat.getMainExecutor(this));
        }

    }

    private void bindCameraUses(){

        int rotation = binding.cameraPreview.getDisplay().getRotation();
        CameraSelector cameraSelector = new CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(binding.cameraPreview.getDisplay().getRotation())
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy ->{

            try (imageProxy) {
                if (reusableBitmap == null || reusableBitmap.getHeight() != imageProxy.getHeight() || reusableBitmap.getWidth() != imageProxy.getWidth()) {
                    reusableBitmap = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
                }
                ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                buffer.rewind();
                reusableBitmap.copyPixelsFromBuffer(buffer);

                Matrix matrix = new Matrix();
                matrix.postRotate((float) imageProxy.getImageInfo().getRotationDegrees());

                if (isfrontCamera) {
                    matrix.postScale(
                            -1f,
                            -1f,
                            imageProxy.getWidth(),
                            imageProxy.getHeight()
                    );
                }
                Bitmap rotatedBitmap = Bitmap.createBitmap(reusableBitmap, 0, 0, reusableBitmap.getWidth(), reusableBitmap.getHeight(),
                        matrix, true);

                detector.detect(rotatedBitmap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        cameraProvider.unbindAll();

        try{
            camera = cameraProvider.bindToLifecycle(
                    ScanActivity.this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
            );
            preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
        }catch (Exception e){
            Log.e("Error", "Use case binding failed");
        }
        }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        detector.clear();

        if(cameraProvider != null){
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!isCameraInitialized ){
            cameraStart();
        }else{
            cameraStart();
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        if(cameraProvider != null){
            cameraProvider.unbindAll();
            isCameraInitialized = false;
        }
    }

    public void onDetect(List<BoundingBox> boundingBoxes){
        Log.i("Detected", "Object Detected");
        overlay.setResults(boundingBoxes);
    }
    public void onEmptyDetect(){
        Log.i("Detected", "No object detected");
        overlay.Clear();
    }

    @Override
    public void OnModelSetupFailed(Exception exception) {
    }

    @Override
    public void OnModelSetupSuccess() {

    }
}