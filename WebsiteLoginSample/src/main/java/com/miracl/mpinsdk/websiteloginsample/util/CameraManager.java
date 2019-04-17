package com.miracl.mpinsdk.websiteloginsample.util;

import android.hardware.Camera;

public class CameraManager {


    static {
        initBackFacingCameraId();
    }

    public static final int INVALID_CAMERA_ID = -1;

    private static int    sCameraId;
    private static Camera sCamera;

    public static void releaseCamera() {
        if (sCamera != null) {
            sCamera.stopPreview();
            sCamera.setPreviewCallback(null);
            sCamera.release();
            sCamera = null;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        if (sCamera == null) {
            try {
                if (sCameraId != INVALID_CAMERA_ID) {
                    sCamera = Camera.open(sCameraId);
                } else {
                    sCamera = Camera.open();
                }
            } catch (Exception e) {
                //Nothing to do
            }
        }

        return sCamera;
    }

    private static void initBackFacingCameraId() {
        sCameraId = INVALID_CAMERA_ID;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                sCameraId = i;
                break;
            }
        }
    }

    public static void setDisplayOrientation(int orientation) {
        if (sCamera != null) {
            sCamera.setDisplayOrientation(orientation);
        }
    }

    public static int getCameraId() {
        return sCameraId;
    }
}