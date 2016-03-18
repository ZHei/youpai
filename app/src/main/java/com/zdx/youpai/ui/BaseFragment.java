package com.zdx.youpai.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Map;

public class BaseFragment extends Fragment {
	public Map<String, String> map;
    public String tag = this.getClass().getSimpleName(); // tag 用于测试log用
	public Context context; // 存储上下文对象
	public Activity activity; // 存储上下文对象
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		activity = getActivity();
	}
}