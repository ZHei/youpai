package com.zdx.youpai.ui.picture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.zdx.youpai.R;
import com.zdx.youpai.utils.CamParaUtil;
import com.zdx.youpai.utils.CameraHelper;
import com.zdx.youpai.utils.GPUImageFilterTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class TakePhotoActivity extends Activity implements OnSeekBarChangeListener, OnClickListener {

    private final String TAG = "TakePhotoActivity";
    private GPUImage mGPUImage;
    private GPUImageFilter mFilter;
    private CameraHelper mCameraHelper;
    private CameraLoader mCameraLoader;

    private GLSurfaceView glS;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏

        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        findViewById(R.id.button_choose_filter).setOnClickListener(this);
        findViewById(R.id.button_takepic).setOnClickListener(this);
        glS = (GLSurfaceView) findViewById(R.id.surfaceView);

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(glS);
//        Log.e(TAG,"gls加载成功");
        mCameraHelper = new CameraHelper(this);
        mCameraLoader = new CameraLoader();

        View cameraSwitchView = findViewById(R.id.img_switch_camera);// 前后摄像头的转换
        cameraSwitchView.setOnClickListener(this);

        // 判断是否存在前后摄像头
        if (CameraInfo.CAMERA_FACING_FRONT != 1 || CameraInfo.CAMERA_FACING_BACK != 0) {
            cameraSwitchView.setVisibility(View.GONE);// 设置不可见
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraLoader.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraLoader.onPause();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_choose_filter:
                GPUImageFilterTools.showDialog(this,
                        new GPUImageFilterTools.OnGpuImageFilterChosenListener() {

                            @Override
                            public void onGpuImageFilterChosenListener(
                                    final GPUImageFilter filter) {
                                switchFilterTo(filter);
                            }
                        });
                break;

            case R.id.button_takepic:
                // 两种对焦方式，首选FOCUS_MODE_CONTINUOUS_PICTURE，强制对焦
                if (mCameraLoader.mCamera.getParameters().getFocusMode()
                        .equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    takePicture();

                } else {
                    mCameraLoader.mCamera
                            .autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(final boolean success,
                                                        final Camera camera) {
                                    takePicture();
                                }
                            });
                }
                break;
            case R.id.img_switch_camera:
                mCameraLoader.switchCamera();
                break;
        }
    }

    // 选择过滤器并设置给mGPUImage
    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(
                filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
            // 给过滤器设置控制滑块的适配器
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    // 效果划条的数值监控
    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress,
                                  final boolean fromUser) {
        if (mFilterAdjuster != null) {
            mFilterAdjuster.adjust(progress);// 传给适配器划条百分比数值
        }
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
    }

    private class CameraLoader {
        private int mCameraId = 0;// 用以区别前后摄像头

        private Camera mCamera;// 设备摄像头

        public void onResume() {
            setUpCamera(mCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        //切换前后摄像头
        public void switchCamera() {
            releaseCamera();
            mCameraId = (mCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCameraId);
        }

        @SuppressLint("InlinedApi")
        private void setUpCamera(final int id) {
            mCamera = getCamera(id);// 实例化摄像机设备
            Parameters parameters = mCamera.getParameters(); // 当前摄像机参数
            Size previewSize = null;
//            if (id == 0)
//                //后置camera预览的最小分辨率宽度为1280，若设备不支持则以最大分辨率
//                previewSize = CamParaUtil.getInstance().getPropPreviewSize(
//                        parameters.getSupportedPreviewSizes(), 1.33f, 1280);
//            else
//                //前置camera预览的最小分辨率宽度为1280，若设备不支持则以最大分辨率
//                previewSize = CamParaUtil.getInstance().getPropPreviewSize(
//                        parameters.getSupportedPreviewSizes(), 1.33f, 720);
//            // 摄像机的预览尺寸参数
//            parameters.setPreviewSize(previewSize.width, previewSize.height);

            if (parameters.getSupportedFocusModes().contains(
                    Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                //强制连续对焦模式
                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            mCamera.setParameters(parameters);

            CameraInfo info = new CameraInfo();
            //获取camera的信息
            Camera.getCameraInfo(id, info);
            int orientation = mCameraHelper.getCameraDisplayOrientation(TakePhotoActivity.this, info);//摄像机的角度
//            Log.v(TAG, String.valueOf(orientation));
            //摄像机方向
            boolean flipHorizontal = info.facing == CameraInfo.CAMERA_FACING_FRONT ? true : false;
//            Log.v(TAG, String.valueOf(flipHorizontal));
            //开启camera
            mGPUImage.setUpCamera(mCamera, orientation, flipHorizontal, false);
        }

        private Camera getCamera(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            try {
                mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    private void takePicture() {
        Parameters parameters = mCameraLoader.mCamera.getParameters();

        //图片的最小分辨率宽度为1280，若设备不支持则以最大分辨率
        Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                parameters.getSupportedPictureSizes(), 1.33f, 1280);
        parameters.setRotation(90);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        mCameraLoader.mCamera.setParameters(parameters);

        mCameraLoader.mCamera.takePicture(null, null,
                new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
                        // 获取原始图片
                        final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                            return;
                        }
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                        } catch (IOException e) {
                        }

                        data = null;//释放data
                        //生成原始图片
                        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());

                        final GLSurfaceView view = (GLSurfaceView) findViewById(R.id.surfaceView);
                        // 拍照的瞬间不实时渲染
                        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//                        Date date = new Date();
//                        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy年MM月HH日");
                        String label = DateUtils.formatDateTime(
                                TakePhotoActivity.this,
                                System.currentTimeMillis(),
                                DateUtils.FORMAT_SHOW_YEAR);
                        mGPUImage.saveToPictures(bitmap, "GPUImage/"+label, System.currentTimeMillis() + ".jpg",
                                new OnPictureSavedListener() {

                                    @Override
                                    public void onPictureSaved(final Uri uri) {
                                        pictureFile.delete();// 拍摄完成后删除“MyCameraApp”的文件
                                        camera.startPreview();// 相机启动预览
                                        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);// 恢复实时
                                    }
                                });
                    }
                });
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /*
    * 1.在根目录下的picture路径下创建原始图片存放文件夹
    * 2.根据当前时间创建原始图片文件
    * */
    private static File getOutputMediaFile(final int type) {
        // 图片存放的标准目录,取一个外部存储器目录来摆放某些类型的文件MyCameraApp存放原图
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // 创建图片存放目录
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.v("MyCameraApp", "创建原始图片存放路径失败");
                return null;
            }
        }
        //以当前时间格式创建目标文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /*
     * SD卡存放图片
     * */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }
    //    private void initMediaRecorder() {
//        if (mMediaRecorder == null) {
//            mMediaRecorder = new MediaRecorder(); // Create MediaRecorder
//        } else
//            mMediaRecorder.reset();
//
//        Log.v("System.outs", "111");
//        mCamera.mCameraInstance.unlock();
//        Log.v("System.outs", "33");
//        // 给Recorder设置Camera对象，保证录像跟预览的方向保持一致
//        mMediaRecorder.setCamera(mCamera.mCameraInstance);
//        // 这两项需要放在setOutputFormat之前
//        Log.v("System.outs", "22");
//        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置从麦克风采集声音
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 设置从摄像头采集图像
//        // mMediaRecorder.setOrientationHint(90);
//        // //改变保存后的视频文件播放时是否横屏(不加这句，视频文件播放的时候角度是反的)
//
//        mMediaRecorder.setProfile(CamcorderProfile
//                .get(CamcorderProfile.QUALITY_HIGH));
//        mMediaRecorder.setVideoSize(640, 480);// 设置要捕获的视频的宽度和高度
//        mMediaRecorder.setVideoFrameRate(20);// 设置要捕获的视频帧速率
//        //mMediaRecorder.setPreviewDisplay(glS.getSurface());
//
//        // 设置视频存储路径
//        String path = getSDPath();
//        if (path != null) {
//
//            File dir = new File(path + "/recordtest");
//            if (!dir.exists()) {
//                dir.mkdir();
//            }
//            path = dir + "/" + getDate() + ".mp4";
//            mMediaRecorder.setOutputFile(path);
//        }
//    }
//
//    private void startRecording() {
//        try {
//            mMediaRecorder.prepare();
//            mMediaRecorder.start();
//        } catch (Exception e) {
//            mStartedFlg = false;
//            Log.e("ASDF", e.getMessage());
//        }
//        mStartedFlg = true;
//    }
//
//    private void stopRecording() {
//        mMediaRecorder.setOnErrorListener(null);// 防止start stop太快闪退
//        mMediaRecorder.setOnInfoListener(null);
//        mMediaRecorder.stop();
//        mMediaRecorder.release();
//        mMediaRecorder = null;
//        mStartedFlg = false;
//        // 重启预览
//        // startPreview();
//        mCamera.mCameraInstance.startPreview();
//    }
//    public static String getDate() {
//        Calendar ca = Calendar.getInstance();
//        int year = ca.get(Calendar.YEAR); // 获取年份
//        int month = ca.get(Calendar.MONTH); // 获取月份
//        int day = ca.get(Calendar.DATE); // 获取日
//        int minute = ca.get(Calendar.MINUTE); // 分
//        int hour = ca.get(Calendar.HOUR); // 小时
//        int second = ca.get(Calendar.SECOND); // 秒
//
//        String date = "" + year + (month + 1) + day + hour + minute + second;
//        Log.d("ASDF", "date:" + date);
//
//        return date;
//    }

}
