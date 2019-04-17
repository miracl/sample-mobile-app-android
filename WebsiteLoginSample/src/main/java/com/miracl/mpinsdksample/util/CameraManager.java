/* **************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************/
package com.miracl.mpinsdksample.util;

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