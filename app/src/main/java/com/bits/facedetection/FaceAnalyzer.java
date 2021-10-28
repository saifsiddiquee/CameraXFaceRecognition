package com.bits.facedetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

/**
 * Created by Saif Siddique on 24 Oct 2021
 */
public class FaceAnalyzer implements ImageAnalysis.Analyzer {

    private Context mContext;

    FaceDetector detector;

    public FaceAnalyzer(Context context) {
        mContext = context;
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees());

            detectFace(image);
        }
        imageProxy.close();
    }

    private void detectFace(InputImage image) {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

        detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(this::drawFace)
                .addOnFailureListener(
                        e -> Log.e("FACES", e.getMessage()));

    }

    private void drawFace(List<Face> faces) {
        for (Face face : faces) {
            Rect rect = face.getBoundingBox();

            Log.d("DATA::",
                    "top: " + rect.top + "\nbottom: " + rect.bottom + "\nleft: " + rect.left
                            + "\nright: "
                            + rect.right);

        }
    }

}
