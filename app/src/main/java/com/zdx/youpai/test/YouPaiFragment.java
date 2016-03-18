package com.zdx.youpai.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.zdx.youpai.R;
import com.zdx.youpai.view.DatePickerFragment;
import com.zdx.youpai.view.TimePickerFragment;

import java.util.Date;
import java.util.UUID;

public class YouPaiFragment extends Fragment {
    public static final String EXTRA_CRIME_ID = "EXTRA_CRIME_ID";
    public static final String DIALOG_DATE = "date";
    public static final String DIALOG_TIME = "time";
    public static final int REQUEST_DATE = 0;
    public static final int REQUEST_TIME = 1;

    private YouPai mYouPai;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    public FragmentManager fm;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
        mYouPai = YouPaiLab.get(getActivity()).getCrime(crimeId);
    }
/*在 onCreateView(...) 方 法 中 ， fragment 的 视 图 是 直 接 通 过 调 用 LayoutInflater.
inflate(...) 方法并传入布局的资源ID生成的。第二个参数是视图的父视图，通常我们需要父
    视图来正确配置组件。第三个参数告知布局生成器是否将生成的视图添加给父视图。这里，我们
   传入了false参数，因为我们将通过activity代码的方式添加视图。*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_youpai, parent, false);
        mTitleField= (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mYouPai.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(
                    CharSequence c, int start, int before, int count) {//CharSequence代表用户输入
                mYouPai.setTitle(c.toString());
            }

            public void beforeTextChanged(
                    CharSequence c, int start, int count, int after) {

            }

            public void afterTextChanged(Editable c) {

            }
        });

        mDateButton= (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fm = getActivity().getSupportFragmentManager();
                chooseDialog();
            }
        });

        mSolvedCheckBox= (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mYouPai.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mYouPai.setSolved(isChecked);
            }
        });
        return v;
    }

    public static YouPaiFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, crimeId);

        YouPaiFragment fragment = new YouPaiFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /*
从fragment中返回结果的处理稍有不同。 fragment能够从activity中接收返回结果，但其自身无法产
生返回结果。 只有activity拥有返回结果。 因此， 尽管Fragment有自己的startActivityForResult(...)
和onActivityResult(...) 方法，但却不具有任何setResult(...) 方法。
相反，我们应通知托管activity返回结果值。具体代码如下：
*/
    public void returnResult(){
        getActivity().setResult(Activity.RESULT_OK, null);//null是intent
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent date){
        if(resultCode != Activity.RESULT_OK) return ;
        if(requestCode == REQUEST_DATE){
            Date d = (Date)date.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mYouPai.setDate(d);
            updateDate();
        }
        if(requestCode == REQUEST_TIME){
            Date d = (Date)date.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mYouPai.setDate(d);
            updateDate();
        }
    }
/*当退出时存储数据*/
    @Override
    public void onPause(){
        super.onPause();
        YouPaiLab.get(getActivity()).saveCrimes();

    }
    public void updateDate(){
        mDateButton.setText(mYouPai.getDateString());
    }

    public void chooseDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择更改");
        builder.setPositiveButton("时刻", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                creatPickerFragment(1);
                dialog.dismiss();
        }
        });
        builder.setNegativeButton("日期", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                creatPickerFragment(0);
                dialog.dismiss();
        }
        });
        builder.create().show();

    }
    public void creatPickerFragment(int i){
        if(i == 0) {
            DatePickerFragment dateDialog = DatePickerFragment.newInstance(mYouPai.getDate());

            dateDialog.setTargetFragment(YouPaiFragment.this, REQUEST_DATE);
            dateDialog.show(fm, DIALOG_DATE);//string参数可唯一识别存放在FragmentManager队列中的DialogFragment
        }
        else {
            TimePickerFragment timeDialog = TimePickerFragment.newInstance(mYouPai.getDate());

            timeDialog.setTargetFragment(YouPaiFragment.this,REQUEST_TIME);
            timeDialog.show(fm,DIALOG_TIME);//string参数可唯一识别存放在FragmentManager队列中的DialogFragment
        }
    }
}

