package com.zdx.youpai.ui.picture;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;


import com.zdx.youpai.R;
import com.zdx.youpai.utils.GPUImageFilterTools;

import java.io.FileNotFoundException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class GalleryPhotoActivity extends Activity implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener, GPUImage.OnPictureSavedListener {

    private static final int REQUEST_PICK_IMAGE = 1;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        findViewById(R.id.button_choose_filter).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);

        mGPUImageView = (GPUImageView) findViewById(R.id.gpuimage);

        //调用图库，获取所有本地图片
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    ContentResolver cr = this.getContentResolver();
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(cr.openInputStream(data.getData()));
                    } catch (FileNotFoundException e) {
                        Log.e("Exception", e.getMessage(), e);
                    }
                    //加载本地图片
                    handleImage(bitmap);
                    //handleImage(data.getData());
                } else {
                    finish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_choose_filter:
                GPUImageFilterTools.showDialog(this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {

                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                        mGPUImageView.requestRender();
                    }

                });
                break;
            case R.id.button_save:
                saveImage();
                break;

            default:
                break;
        }

    }

    @Override
    public void onPictureSaved(final Uri uri) {
        Toast.makeText(this, "保存到: " + uri.toString(), Toast.LENGTH_SHORT).show();
    }

    private void saveImage() {
        String fileName = System.currentTimeMillis() + ".jpg";
        mGPUImageView.saveToPictures("GPUImage", fileName, this);
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImageView.setFilter(mFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
        if (mFilterAdjuster != null) {
            mFilterAdjuster.adjust(progress);
        }
        mGPUImageView.requestRender();
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
    }

    private void handleImage(Bitmap bitmap) {
        //mGPUImageView继承自GLSurfaceView，通过bitmap的方式加载图片
        mGPUImageView.setImage(bitmap);
    }

    private void handleImage(final Uri selectedImage) {
        //mGPUImageView继承自GLSurfaceView，通过url的方式加载图片
        mGPUImageView.setImage(selectedImage);
    }
}

