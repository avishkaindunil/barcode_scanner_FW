package com.example.barcode_scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    private PreviewView previewView;
    private BarcodeOverlayView overlayView;
    private BarcodeScanner barcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlay);

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            setupScannerAndStartCamera();
        }
    }

    private void setupScannerAndStartCamera() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);

        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1080, 2400 )) // or any resolution
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();


                analysis.setAnalyzer(Executors.newSingleThreadExecutor(), this::processImage);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImage(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage inputImage = InputImage.fromMediaImage(mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees());

            int imageWidth = mediaImage.getWidth();
            int imageHeight = mediaImage.getHeight();

            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        List<RectF> mappedRects = new ArrayList<>();
                        for (Barcode barcode : barcodes) {
                            if (barcode.getBoundingBox() != null) {
                                RectF rectF = mapRectToView(barcode.getBoundingBox(), imageWidth, imageHeight);
                                mappedRects.add(rectF);
                            }
                        }
                        overlayView.setMappedRects(mappedRects);
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private RectF mapRectToView(Rect rect, int imageWidth, int imageHeight) {
        float viewHeight = previewView.getWidth();
        float viewWidth = previewView.getHeight();

        // Calculate aspect ratios
        float imageRatio = (float) imageWidth / imageHeight;
        float viewRatio = viewHeight / viewWidth;

        float scaleX, scaleY;
        float offsetX = 0f, offsetY = 0f;

        if (viewRatio > imageRatio) {
            // Preview is wider than camera image → letterbox horizontal
            scaleY = viewHeight / (float) imageHeight;
            scaleX = scaleY;
            float scaledImageWidth = imageWidth * scaleX;
            offsetX = (viewWidth - scaledImageWidth) / 2f;
        } else {
            // Preview is taller than camera image → letterbox vertical
            scaleX = viewWidth / (float) imageWidth;
            scaleY = scaleX;
            float scaledImageHeight = imageHeight * scaleY;
            offsetY = (viewHeight - scaledImageHeight) / 2f;
        }

        // Map rect coordinates
        float left = rect.left * scaleX + offsetX;
        float top = rect.top * scaleY + offsetY;
        float right = rect.right * scaleX + offsetX;
        float bottom = rect.bottom * scaleY + offsetY;

        return new RectF(left, top, right, bottom);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupScannerAndStartCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
