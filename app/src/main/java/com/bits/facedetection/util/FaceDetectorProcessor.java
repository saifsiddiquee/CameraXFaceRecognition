/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bits.facedetection.util;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.List;
import java.util.Locale;

/**
 * Face Detector Demo.
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

    private static final String TAG = "FaceDetectorProcessor";

    private final FaceDetector detector;

    private FaceDetectCallback mFaceDetectCallback;

    public FaceDetectorProcessor(Context context, FaceDetectCallback faceDetectCallback) {
        super(context);
        mFaceDetectCallback = faceDetectCallback;
        detector = FaceDetection.getClient();
    }

    @Override
    public void stop() {
        super.stop();
        detector.close();
    }

    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Face> faces) {
        for (Face face : faces) {
            Rect rect = face.getBoundingBox();

            mFaceDetectCallback.onFaceDetect(rect);

            Log.d("DATA::",
                    "top: " + rect.top + "\nbottom: " + rect.bottom + "\nleft: " + rect.left
                            + "\nright: "
                            + rect.right);
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
