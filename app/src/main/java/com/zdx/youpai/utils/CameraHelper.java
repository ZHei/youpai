package com.zdx.youpai.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Surface;

public class CameraHelper {
    private static final String TAG="CameraHelper";

    public CameraHelper(final Context context) {
    }

    /*
    * 获取设备的camera数量
    * */
    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    /*
    * 开启camera
    * */
    public Camera openCamera(final int id) {
        return Camera.open(id);
    }

    public void setCameraDisplayOrientation(final Activity activity, CameraInfo info, final Camera camera) {
        int result = getCameraDisplayOrientation(activity, info);
        camera.setDisplayOrientation(result);
    }

    /*
    * camera拍摄角度适应设备旋转的方法
    * */
    public int getCameraDisplayOrientation(final Activity activity, CameraInfo info) {
        //获取设备屏幕方向:0-竖屏
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
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
        //info.facing
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.v(TAG, "设备方向:"+String.valueOf(degrees)+
                "camera方向:"+String.valueOf(info.orientation+
                "返回方向:"+String.valueOf(result)));
        return result;
    }
}
