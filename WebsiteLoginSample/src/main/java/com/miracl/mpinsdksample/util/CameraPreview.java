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


import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private volatile boolean mIsPreviewing;
    private volatile boolean mIsCameraReleased;

    private SurfaceHolder   mHolder;
    private PreviewCallback mPreviewCallback;

    public CameraPreview(Context context, PreviewCallback previewCallback) {
        super(context);
        mPreviewCallback = previewCallback;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }


    public boolean isPreviewing() {
        return mIsPreviewing;
    }


    public void startPreviewCallback() {
        if (!isPreviewing()) {
            if (mIsCameraReleased) {
                startCamera();
            }
            Camera camera = CameraManager.getCameraInstance();
            if (camera != null) {
                camera.setPreviewCallback(mPreviewCallback);
                mIsPreviewing = true;
            }
        }
    }

    public void stopPreviewCallback() {
        if (isPreviewing()) {
            Camera camera = CameraManager.getCameraInstance();
            if (camera != null && mIsPreviewing) {
                camera.setPreviewCallback(null);
                mIsPreviewing = false;
            }
        }
    }

    public void startCamera() {
        Camera camera = CameraManager.getCameraInstance();
        if (camera != null) {

            try {
                camera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                //TODO: Handle Exception
            }

            setCameraDisplayOrientation();
            camera.setPreviewCallback(mPreviewCallback);
            setFocusMode(camera);
            camera.startPreview();
            mIsPreviewing = true;
            mIsCameraReleased = false;
        }
    }

    public void releaseCamera() {
        stopPreviewCallback();
        CameraManager.releaseCamera();
        mIsCameraReleased = true;
    }

    private void setFocusMode(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
        }
    }

    private void setCameraDisplayOrientation() {
        int cameraId = CameraManager.getCameraId();
        if (cameraId != CameraManager.INVALID_CAMERA_ID) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            CameraManager.setDisplayOrientation(result);
        }
    }
}