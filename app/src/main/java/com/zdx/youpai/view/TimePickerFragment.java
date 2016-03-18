package com.zdx.youpai.view;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TimePicker;

import com.zdx.youpai.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePickerFragment extends DialogFragment {
    public static final String EXTRA_TIME = "extra_time";
    private Date mDate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDate = (Date) getArguments().getSerializable(EXTRA_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);

        final int year = calendar.get(calendar.YEAR);
        final int month = calendar.get(calendar.MONTH);
        final int day = calendar.get(calendar.DAY_OF_MONTH);
        int hour = calendar.get(calendar.HOUR_OF_DAY);
        int minute = calendar.get(calendar.MINUTE);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_time,null);
        TimePicker timePicker = (TimePicker) v.findViewById(R.id.dialog_time_timepicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mDate = new GregorianCalendar(year,month,day,hourOfDay,minute).getTime();
                //当发生如屏幕旋转产生的销毁fragmen时，用此保存mDate从而在onCreateDialog时可以
                getArguments().putSerializable(EXTRA_TIME,mDate);
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
                }).create();
    }
    public static TimePickerFragment newInstance(Date date){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TIME, date);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void sendResult(int resultCode){
        if(getTargetFragment() == null){
            return;
        }
        Intent i = new Intent();
        i.putExtra(EXTRA_TIME, mDate);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);

    }
}
