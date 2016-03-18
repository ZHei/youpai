package com.zdx.youpai.ui.shared.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.zdx.youpai.test.YouPai;
import com.zdx.youpai.R;

import java.util.ArrayList;


public class SharedListAdapter extends ArrayAdapter<YouPai> {

    public SharedListAdapter(Context context, ArrayList<YouPai> youPais) {
        super(context, 0, youPais);//父类ArrayAdapter的构造函数
    }

    /* 相当于onCreateView
    * convertView参数是一个已存在的列表项， adapter可重新配置并返回它，因此我们无需再创
    *建全新的视图对象
    */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ViewHolder viewHolder;
        YouPai c = getItem(position);
/*不为空则直接对 convertView 进行重用
* ViewHolder减少获取控件*/

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_youpai, null);

            viewHolder = new ViewHolder();
            viewHolder.titleTextView = (TextView) view.findViewById(R.id.crime_list_item_titleTextView);
            viewHolder.dateTextView = (TextView) view.findViewById(R.id.crime_list_item_dateTextView);
            viewHolder.solvedChecked =  (CheckBox) view.findViewById(R.id.crime_list_item_solvedCheckBox);
            view.setTag(viewHolder); // 将ViewHolder存储在View中
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(); // 重新获取ViewHolder
        }

        viewHolder.titleTextView.setText(c.getTitle());
        viewHolder.dateTextView.setText(c.getDateString());
        viewHolder.solvedChecked.setChecked(c.isSolved());

        return view;
    }

    private class ViewHolder {
        TextView titleTextView,dateTextView;
        CheckBox solvedChecked;
    }
}
