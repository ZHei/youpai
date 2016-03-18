package com.zdx.youpai.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zdx.youpai.data.LeftMenuItem;
import com.zdx.youpai.R;

import java.util.ArrayList;

/*
* 使用drawerlayout时采用
* */
public class MenuLeftFragment extends Fragment {

    private ListView mListView;
    public LeftMenuListAdapter adapter;

    private ArrayList<LeftMenuItem> leftMenuItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        initFruits(); // 初始化数据
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_left, container, false);
        mListView = (ListView) view.findViewById(R.id.id_leftmenu_listView);
        adapter = new LeftMenuListAdapter(getActivity(),R.layout.list_item_leftmenu,leftMenuItems);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }
        });
        return view;
    }

    public class LeftMenuListAdapter extends ArrayAdapter<LeftMenuItem> {
        private int resourceId;

        public LeftMenuListAdapter(Context context, int ResourceId,
                                   ArrayList<LeftMenuItem> objects) {
            super(context, ResourceId, objects);
            this.resourceId = ResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView == null){
                view = LayoutInflater.from(getContext()).inflate(this.resourceId, null);
            }
            else{
                view = convertView;
            }
            LeftMenuItem item = getItem(position);
            ImageView fruitImage = (ImageView) view.findViewById(R.id.id_leftmenu_item_iv);
            TextView fruitName = (TextView) view.findViewById(R.id.id_leftmenu_item_tv);
            fruitImage.setImageResource(item.getImageId());
            fruitName.setText(item.getName());
            return view;
        }
    }

    private void initFruits() {
        leftMenuItems = new ArrayList<LeftMenuItem>();
        LeftMenuItem apple = new LeftMenuItem("切换用户", R.drawable.img_1);
        leftMenuItems.add(apple);
        LeftMenuItem banana = new LeftMenuItem("退出登录", R.drawable.img_2);
        leftMenuItems.add(banana);
    }
}