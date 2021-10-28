package com.bits.facedetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.bits.facedetection.databinding.ActivityFaceDetectionCameraBinding;
import com.bits.facedetection.util.FaceDetectCallback;
import com.bits.facedetection.util.FaceDetectorProcessor;
import com.bits.facedetection.util.VisionImageProcessor;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.MlKitException;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Saif Siddique on 24 Oct 2021
 */
public class FaceDetectionCameraActivity extends AppCompatActivity implements
        EasyPermissions.PermissionCallbacks, FaceDetectCallback {
    private static final String TAG = FaceDetectionCameraActivity.class.getSimpleName();

    public static final int RC_CAMERA_PERM = 9001;
    private ActivityFaceDetectionCameraBinding mBinding;

    private VisionImageProcessor mVisionImageProcessor;

    int width;
    int height;
    float ovalLeft;
    float ovalTop;
    float ovalRight;
    float ovalBottom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityFaceDetectionCameraBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        openCamera();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startCamera();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            startCamera();
        }
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    private void openCamera() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            startCamera();
        } else {
            EasyPermissions.requestPermissions(this,
                    "This app needs permissions to take pictures.",
                    RC_CAMERA_PERM, perms);
        }
    }

    private void startCamera() {

        mVisionImageProcessor = new FaceDetectorProcessor(this, this);

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider
                .getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                bindPreview(cameraProvider);

            } catch (Exception exception) {
                Log.d(TAG, "startCamera: " + exception.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        final CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        final ImageCapture.Builder builder = new ImageCapture.Builder();

        final ImageCapture imageCapture = builder
                .build();

        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation;
                // Monitors orientation values to determine the target rotation value
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }

                imageCapture.setTargetRotation(rotation);
            }
        };

        orientationEventListener.enable();

        width = mBinding.imageFrame.getWidth();
        height = mBinding.imageFrame.getHeight();

        ovalLeft = mBinding.imageFrame.getLeft();
        ovalRight = mBinding.imageFrame.getRight();

        ovalTop = mBinding.imageFrame.getTop();
        ovalBottom = mBinding.imageFrame.getBottom();

        Log.d("DATA::",
                "\ntop: " + ovalTop + "\nbottom: " + ovalBottom + "\nleft: " + ovalLeft
                        + "\nright: "
                        + ovalRight);

        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(mBinding.previewView.getSurfaceProvider());

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int targetWidth = size.x;
        int targetHeight = size.y;


        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(targetWidth, targetHeight))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    try {
                        mVisionImageProcessor.processImageProxy(imageProxy);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    @Override
    public void onFaceDetect(Rect rect) {
        if (rect.left < ovalLeft &&
                rect.top > ovalTop &&
                rect.bottom < ovalBottom &&
                rect.right < ovalRight) {

            mBinding.imageFrame.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.shape_oval_success));
        } else {
            mBinding.imageFrame.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.shape_oval_default));
        }
    }
}
