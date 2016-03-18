package com.zdx.youpai.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.zdx.youpai.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/*
* 不使用DialogFragment，也可显示AlertDialog视图，但Android开发原则不推荐这种做法。使用
*FragmentManager管理对话框，可使用更多配置选项来显示对话框。*/

public class DatePickerFragment extends DialogFragment {
    public static final String EXTRA_DATE = "extra_date";
    private Date mDate;

    /*
    * 当为普通的Fragment之间数据传递时使用Fragment.starActivity和onActivityResult(...) 方法，
    * 被启动的Fragment调用托管Activity的getActivity.setResult(...) 方法
    * 特殊的如DialogFragment用设置目标Fragment的方式，调用show
    */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /*采用布局来生成日期选择对话框
        DatePicker datePicker = new DatePicker(getActivity()); setView(datePicker)
        也可以但不灵活
        */
        mDate = (Date) getArguments().getSerializable(EXTRA_DATE);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH);
        int day = calendar.get(calendar.DAY_OF_MONTH);
        final int hour = calendar.get(calendar.HOUR_OF_DAY);
        final int minute = calendar.get(calendar.MINUTE);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_date, null);
        DatePicker datePicker = (DatePicker) v.findViewById(R.id.dialog_date_datepicker);
        /*隐藏日期选择器的日*/
        ((ViewGroup) (((ViewGroup) datePicker.getChildAt(0)).getChildAt(0)))
                .getChildAt(2).setVisibility(View.GONE);
        //初始化并设定监听器
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //更新mDate
                mDate = new GregorianCalendar(year,monthOfYear,dayOfMonth,hour,minute).getTime();
                //当发生如屏幕旋转产生的销毁fragmen时，用此保存mDate从而在onCreateDialog时可以
                getArguments().putSerializable(EXTRA_DATE,mDate);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                }).create();//一个字符串资源以及一个实现DialogInterface.OnClickListener接口的对象
    }

    public static DatePickerFragment newInstance(Date date){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void sendResult(int resultCode){
        if(getTargetFragment() == null){
            return;
        }
        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, mDate);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,i);
    }
}

