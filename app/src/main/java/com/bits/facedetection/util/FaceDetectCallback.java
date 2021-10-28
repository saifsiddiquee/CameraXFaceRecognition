package com.bits.facedetection.util;

import android.graphics.Rect;

/**
 * Created by Saif Siddique on 28 Oct 2021
 */
public interface FaceDetectCallback {
    void onFaceDetect(Rect rect);
}
