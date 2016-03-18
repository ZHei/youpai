package com.zdx.youpai.ui.vedio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.MediaRecorderBase;

import com.yixia.weibo.sdk.MediaRecorderNative;
import com.yixia.weibo.sdk.MediaRecorderSystem;
import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.model.MediaObject;
import com.yixia.weibo.sdk.model.MediaObject$MediaPart;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;
import com.zdx.youpai.R;
import com.zdx.youpai.common.CommonIntentExtra;
import com.zdx.youpai.ui.vedio.views.ProgressView;

import java.io.File;

/**
 * 视频录制
 *
 */
public class MediaRecorderActivity extends VedioBaseActivity implements com.yixia.weibo.sdk.MediaRecorderBase$OnErrorListener, OnClickListener, com.yixia.weibo.sdk.MediaRecorderBase$OnPreparedListener, com.yixia.weibo.sdk.MediaRecorderBase$OnEncodeListener {

	/** 录制最长时间 */
	public final static int RECORD_TIME_MAX = 10 * 1000;
	/** 录制最小时间 */
	public final static int RECORD_TIME_MIN = 3 * 1000;
	/** 刷新进度条 */
	private static final int HANDLE_INVALIDATE_PROGRESS = 0;
	/** 延迟拍摄停止 */
	private static final int HANDLE_STOP_RECORD = 1;

	/** 下一步 */
	private ImageView mTitleNext;
	/** 前后摄像头切换 */
	private CheckBox mCameraSwitch;
	/** 回删按钮、延时按钮、滤镜按钮 */
	private CheckedTextView mRecordDelete;
	/** 闪光灯 */
	private CheckBox mRecordLed;
	/** 拍摄按钮 显示状态变换*/
	private ImageView mRecordController;
	/** 导入视频*/
	private ImageView mImportVideo;
	/** 底部条 */
	private RelativeLayout mBottomLayout;
	/** 摄像头数据显示画布 */
	private SurfaceView mSurfaceView;
	/** 录制进度 */
	private ProgressView mProgressView;

	/** SDK视频录制对象 */
	private MediaRecorderBase mMediaRecorder;
	/** 视频信息 */
	private MediaObject mMediaObject;

	/** 需要重新编译（拍摄新的或者回删） */
	private boolean mRebuild;
	/** 状态开启 */
	private boolean mCreated;
	/** 是否是点击状态 */
	private volatile boolean mPressedStatus;
	/** 是否已经释放 */
	private volatile boolean mReleased;
	/** 底部背景色 */
	private int mBackgroundColorNormal, mBackgroundColorPress;
	/** 屏幕宽度 */
	private int mWindowWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mCreated = false;
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
		loadIntent();
		loadViews();
		mCreated = true;

	}

	/** 加载传入的参数 */
	private void loadIntent() {
		mWindowWidth = DeviceUtils.getScreenWidth(this);

		mBackgroundColorNormal = getResources().getColor(R.color.black);
		mBackgroundColorPress = getResources().getColor(R.color.camera_bottom_press_bg);
	}

	/** 加载视图 */
	private void loadViews() {
		setContentView(R.layout.activity_media_recorder);

		mSurfaceView = (SurfaceView) findViewById(R.id.record_preview);
		mCameraSwitch = (CheckBox) findViewById(R.id.record_camera_switcher);
		mTitleNext = (ImageView) findViewById(R.id.title_next);
		mProgressView = (ProgressView) findViewById(R.id.record_progress);
		mRecordDelete = (CheckedTextView) findViewById(R.id.record_delete);
		mRecordController = (ImageView) findViewById(R.id.record_controller);
		mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
		mRecordLed = (CheckBox) findViewById(R.id.record_camera_led);
		mImportVideo = (ImageView) findViewById(R.id.importVideo_btn);

		mImportVideo.setOnClickListener(this);
        //ICS-SDK版本大于14时设置预览屏幕的触摸事件
//		if (DeviceUtils.hasICS()) {
//			mSurfaceView.setOnTouchListener(mOnSurfaveViewTouchListener);
//		}

		mTitleNext.setOnClickListener(this);
		findViewById(R.id.title_back).setOnClickListener(this);
		mRecordDelete.setOnClickListener(this);
		mBottomLayout.setOnTouchListener(mOnVideoControllerTouchListener);


		//是否支持前置摄像头
		if (MediaRecorderBase.isSupportFrontCamera()) {
			mCameraSwitch.setOnClickListener(this);
		} else {
			mCameraSwitch.setVisibility(View.GONE);
		}
		//是否支持闪光灯
		if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
			mRecordLed.setOnClickListener(this);
		} else {
			mRecordLed.setVisibility(View.GONE);
		}
        //设置最大拍摄长度条
		mProgressView.setMaxDuration(RECORD_TIME_MAX);
//		initSurfaceView();
	}

	/** 初始化画布 */
	private void initSurfaceView() {
		final int w = DeviceUtils.getScreenWidth(this);
        /*设备窗口自定义预览窗口*/
//		((RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams()).topMargin = w;
//		int width = w;
//		int height = w * 4 / 3;
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
		lp.width = w;
		lp.height = w * 4 / 3;
		mSurfaceView.setLayoutParams(lp);
	}

	/** 初始化拍摄SDK */
	private void initMediaRecorder() {
        //默认录制模式
		mMediaRecorder = new MediaRecorderNative();
		mRebuild = true;

        //录制错误监听
		mMediaRecorder.setOnErrorListener(this);
        //转码监听
		mMediaRecorder.setOnEncodeListener(this);
		File f = new File(VCamera.getVideoCachePath());
		if (!FileUtils.checkFile(f)) {
			f.mkdirs();
		}
		String key = String.valueOf(System.currentTimeMillis());
        String label = DateUtils.formatDateTime(
                MediaRecorderActivity.this,
                System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_YEAR);
        //以当前系统时间名新建缓存目录
		mMediaObject = mMediaRecorder.setOutputDirectory(key, VCamera.getVideoCachePath() +label+"/"+ key);
        //设置预览的surfaceview
//		mMediaRecorder.setOnSurfaveViewTouchListener(mSurfaceView);
		mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
		mMediaRecorder.prepare();
	}

	/** 点击预览屏幕对焦(不用) */
//	private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {
//
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			if (mMediaRecorder == null || !mCreated) {
//				return false;
//			}
//
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				//检测是否手动对焦
//				showFocusImage(event);
//				if (!mMediaRecorder.onTouch(event, new AutoFocusCallback() {
//					@Override
//					public void onAutoFocus(boolean success, Camera camera) {
//						mFocusImage.setVisibility(View.GONE);
//
//					}
//				})) {
//					return true;
//				} else {
//					mFocusImage.setVisibility(View.GONE);
//				}
//				mMediaRecorder.setAutoFocus();
//				break;
//			}
//			return true;
//		}
//
//	};

	/** 点击底部屏幕录制 */
	private View.OnTouchListener mOnVideoControllerTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mMediaRecorder == null) {
				return false;
			}
			switch (event.getAction()) {
			    case MotionEvent.ACTION_DOWN:
				    //判断是否已经超时
//                    Logger.e("[MediaRecorderActivity]mMediaObject.getDuration() " + mMediaObject.getDuration());
				    if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
					    return true;
				        }
				    //取消回删
				    if (cancelDelete())
					    return true;
                    startRecord();
				    break;
			    case MotionEvent.ACTION_UP:
				    // 暂停
				    if (mPressedStatus) {
					    stopRecord();
					    //检测是否已经完成
					    if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                            //时间达到，自动执行点击事件
						    mTitleNext.performClick();
                        }
				    }
				    break;
			    }
			    return true;
		}

	};

	@Override
	public void onResume() {
		super.onResume();
        //主题滤镜释放初始化
		UtilityAdapter.freeFilterParser();
		UtilityAdapter.initFilterParser();

		if (mMediaRecorder == null) {
			initMediaRecorder();
		} else {
			mRecordLed.setChecked(false);
			mMediaRecorder.prepare();
			mProgressView.setData(mMediaObject);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopRecord();
		UtilityAdapter.freeFilterParser();
		if (!mReleased) {
			if (mMediaRecorder != null)
				mMediaRecorder.release();
		}
		mReleased = false;
	}

	/** 开始录制
     * MediaObject$MediaPart:分段录制*/
	private void startRecord() {
		if (mMediaRecorder != null) {
			com.yixia.weibo.sdk.model.MediaObject$MediaPart part = mMediaRecorder.startRecord();
			if (part == null) {
				return;
			}

			//如果使用MediaRecorderSystem，不能在中途切换前后摄像头，否则有问题
			if (mMediaRecorder instanceof MediaRecorderSystem) {
				mCameraSwitch.setVisibility(View.GONE);
			}
            //
			mProgressView.setData(mMediaObject);
		}

		mRebuild = true;
		mPressedStatus = true;
		mRecordController.setImageResource(R.drawable.record_controller_press);
		mBottomLayout.setBackgroundColor(mBackgroundColorPress);

		if (mHandler != null) {
			mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
			mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);

			mHandler.removeMessages(HANDLE_STOP_RECORD);
			mHandler.sendEmptyMessageDelayed(HANDLE_STOP_RECORD, RECORD_TIME_MAX - mMediaObject.getDuration());
		}
		mRecordDelete.setVisibility(View.GONE);
		mCameraSwitch.setEnabled(false);
		mRecordLed.setEnabled(false);
	}

	@Override
	public void onBackPressed() {
        //当回删按钮激活时后退键退出回删状态
		if (mRecordDelete != null && mRecordDelete.isChecked()) {
			cancelDelete();
			return;
		}

		if (mMediaObject != null && mMediaObject.getDuration() > 1) {
			//未转码
			new AlertDialog.Builder(this).setTitle(R.string.hint).setMessage(R.string.record_camera_exit_dialog_message).setNegativeButton(R.string.record_camera_cancel_dialog_yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mMediaObject.delete();
					finish();
				}

			}).setPositiveButton(R.string.record_camera_cancel_dialog_no, null).setCancelable(false).show();
			return;
		}
		//停止消息推送轮询，释放camere
		VCamera.stopPollingService();
		finish();
	}

	/** 停止录制 */
	private void stopRecord() {
		mPressedStatus = false;
		mRecordController.setImageResource(R.drawable.record_controller_normal);
		mBottomLayout.setBackgroundColor(mBackgroundColorNormal);

		if (mMediaRecorder != null) {
			mMediaRecorder.stopRecord();
		}

		mRecordDelete.setVisibility(View.VISIBLE);
		mCameraSwitch.setEnabled(true);
		mRecordLed.setEnabled(true);

		mHandler.removeMessages(HANDLE_STOP_RECORD);
		checkStatus();//检查录制时间
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (mHandler.hasMessages(HANDLE_STOP_RECORD)) {
			mHandler.removeMessages(HANDLE_STOP_RECORD);
		}

		//处理开启回删后其他点击操作
		if (id != R.id.record_delete) {
			if (mMediaObject != null) {
				com.yixia.weibo.sdk.model.MediaObject$MediaPart part = mMediaObject.getCurrentPart();
				if (part != null) {
					if (part.remove) {
						part.remove = false;
						mRecordDelete.setChecked(false);
						if (mProgressView != null)
                            //回删进度条的一段
							mProgressView.invalidate();
					}
				}
			}
		}

		switch (id) {
		    case R.id.title_back:
			    onBackPressed();
			    break;
		    case R.id.record_camera_switcher:// 前后摄像头切换
			    if (mRecordLed.isChecked()) {
				    if (mMediaRecorder != null) {
					    mMediaRecorder.toggleFlashMode();
				    }
				    mRecordLed.setChecked(false);
			    }

			    if (mMediaRecorder != null) {
				    mMediaRecorder.switchCamera();
			    }

			    if (mMediaRecorder.isFrontCamera()) {
				    mRecordLed.setEnabled(false);
			    } else {
				    mRecordLed.setEnabled(true);
			    }
			    break;
		    case R.id.record_camera_led:
			    //开启前置摄像头以后不支持开启闪光灯
			    if (mMediaRecorder != null) {
				    if (mMediaRecorder.isFrontCamera()) {
					    return;
				    }
			    }

			    if (mMediaRecorder != null) {
				    mMediaRecorder.toggleFlashMode();
			    }
			    break;
		    case R.id.title_next:
                //调用转码接口
			    mMediaRecorder.startEncoding();
			    break;
		    case R.id.record_delete:
			    //取消回删
			    if (mMediaObject != null) {
				    com.yixia.weibo.sdk.model.MediaObject$MediaPart part = mMediaObject.getCurrentPart();
				    if (part != null) {
					    if (part.remove) {
						    mRebuild = true;
						    part.remove = false;
						    backRemove();
						    mRecordDelete.setChecked(false);
					    } else {
						    part.remove = true;
						    mRecordDelete.setChecked(true);
					    }
				    }
				    if (mProgressView != null)
					    mProgressView.invalidate();
				    //检测按钮状态
				    checkStatus();
			    }
			    break;
		    case R.id.importVideo_btn:
			    startActivity(new Intent(MediaRecorderActivity.this, ImportVideoFolderActivity.class));
			    break;
        }
    }

	/** 回删 */
	public boolean backRemove() {
		if (mMediaObject != null && mMediaObject.mediaList != null) {
			int size = mMediaObject.mediaList.size();
			if (size > 0) {
				com.yixia.weibo.sdk.model.MediaObject$MediaPart part = (MediaObject$MediaPart) mMediaObject.mediaList.get(size - 1);
				mMediaObject.removePart(part, true);

				if (mMediaObject.mediaList.size() > 0)
					mMediaObject.mCurrentPart = (MediaObject$MediaPart) mMediaObject.mediaList.get(mMediaObject.mediaList.size() - 1);
				else
					mMediaObject.mCurrentPart = null;
				return true;
			}
		}
		return false;
	}

	/** 取消回删 */
	private boolean cancelDelete() {
		if (mMediaObject != null) {
			MediaObject$MediaPart part = mMediaObject.getCurrentPart();
			if (part != null && part.remove) {
				part.remove = false;
				mRecordDelete.setChecked(false);

				if (mProgressView != null)
					mProgressView.invalidate();

				return true;
			}
		}
		return false;
	}

	/** 检查录制时间，显示/隐藏下一步按钮 */
	private int checkStatus() {
		int duration = 0;
		if (!isFinishing() && mMediaObject != null) {
			duration = mMediaObject.getDuration();
			if (duration < RECORD_TIME_MIN) {
				if (duration == 0) {
					mCameraSwitch.setVisibility(View.VISIBLE);
					mRecordDelete.setVisibility(View.GONE);
				}
				//视频必须大于3秒
				if (mTitleNext.getVisibility() != View.INVISIBLE)
					mTitleNext.setVisibility(View.INVISIBLE);
			} else {
				//下一步
				if (mTitleNext.getVisibility() != View.VISIBLE) {
					mTitleNext.setVisibility(View.VISIBLE);
				}
			}
		}
		return duration;
	}


	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_STOP_RECORD:
				stopRecord();
				mTitleNext.performClick();
				break;
			case HANDLE_INVALIDATE_PROGRESS:
				if (mMediaRecorder != null && !isFinishing()) {
					if (mProgressView != null)
						mProgressView.invalidate();
					//					if (mPressedStatus)
					//						titleText.setText(String.format("%.1f", mMediaRecorder.getDuration() / 1000F));
					if (mPressedStatus)
						sendEmptyMessageDelayed(0, 30);
				}
				break;
			}
		}
	};

    @Override
    public void onEncodeStart() {
        showProgress("", getString(R.string.record_camera_progress_message));
    }

    @Override
    public void onEncodeProgress(int progress) {
        //		Logger.e("[MediaRecorderActivity]onEncodeProgress..." + progress);
    }

    /** 转码完成 */
    @Override
    public void onEncodeComplete() {
        hideProgress();
        Intent intent = new Intent(this, MediaPreviewActivity.class);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            bundle = new Bundle();
        bundle.putSerializable(CommonIntentExtra.EXTRA_MEDIA_OBJECT, mMediaObject);
        //视频存储路径
        bundle.putString("output", mMediaObject.getOutputTempVideoPath());
//        Log.e("ha", mMediaObject.getOutputTempVideoPath());
        bundle.putBoolean("Rebuild", mRebuild);//是否外部导入
        intent.putExtras(bundle);
        startActivity(intent);
        mRebuild = false;
    }

    /**
     * 转码失败
     * 	检查sdcard是否可用，检查分块是否存在
     */
    @Override
    public void onEncodeError() {
        hideProgress();
        Toast.makeText(this, R.string.record_video_transcoding_faild, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVideoError(int what, int extra) {

    }

    @Override
    public void onAudioError(int what, String message) {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
