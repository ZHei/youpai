package com.zdx.youpai.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.zdx.youpai.test.YouPai;
import com.zdx.youpai.test.YouPaiLab;
import com.zdx.youpai.ui.picture.TakePhotoActivity;
import com.zdx.youpai.ui.picture.GalleryPhotoActivity;
import com.zdx.youpai.test.ActivityYouPaiPager;
import com.zdx.youpai.test.YouPaiFragment;
import com.zdx.youpai.R;
import com.zdx.youpai.ui.vedio.MediaRecorderActivity;

public class AddPopupWindow extends PopupWindow {

    private LinearLayout mTakePic,mTakeMove,mSelectPic,mSelectMove;
	private View mMenuView;
	public int w,h;
    private Context mConext;

	public AddPopupWindow(final Activity context) {
		super(context);
        mConext = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.dialog_addpop, null);

		h = context.getWindowManager().getDefaultDisplay().getHeight();
		w = context.getWindowManager().getDefaultDisplay().getWidth();
		//设置按钮监听
        mTakePic = (LinearLayout) mMenuView.findViewById(R.id.take_picture);
        mTakeMove = (LinearLayout) mMenuView.findViewById(R.id.take_move);
        mSelectPic = (LinearLayout) mMenuView.findViewById(R.id.select_picture);
        mSelectMove = (LinearLayout) mMenuView.findViewById(R.id.select_move);
        mTakePic.setOnClickListener(new tabListener());
        mTakeMove.setOnClickListener(new tabListener());
        mSelectPic.setOnClickListener(new tabListener());
        mSelectMove.setOnClickListener(new tabListener());

		//设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(w);
		//设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.mystyle);
		//实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(01);
		//设置SelectPicPopupWindow弹出窗体的背景，才能点击窗体外销毁弹框
		this.setBackgroundDrawable(dw);
		/* 触摸popupwindow外部，popupwindow消失。这个要求你的popupwindow要有背景图片才可以成功，如上*/
		this.setOutsideTouchable(true);

	}
    public class tabListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                    mConext.startActivity(intent);
                    mConext.startActivity(new Intent(mConext,TakePhotoActivity.class));
                    break;
                case R.id.take_move:

                    mConext.startActivity(new Intent(mConext,MediaRecorderActivity.class));
                    break;
                case R.id.select_picture:
                    mConext.startActivity(new Intent(mConext,GalleryPhotoActivity.class));
                    break;
                case R.id.select_move:
                    YouPai yp = new YouPai();
                    YouPaiLab.get(mConext).addCrime(yp);
                    Intent i2 = new Intent(mConext, ActivityYouPaiPager.class);
                    i2.putExtra(YouPaiFragment.EXTRA_CRIME_ID, yp.getId());
                    mConext.startActivity(i2);
                    break;
                default:
                    break;
            }
        }
    }
}
