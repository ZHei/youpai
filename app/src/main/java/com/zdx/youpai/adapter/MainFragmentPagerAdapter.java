package com.zdx.youpai.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
	ArrayList<Fragment> mlist;
	public MainFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {
		super(fm);
		this.mlist = list;
	}

	@Override
	public int getCount() {
		return mlist.size();
	}

	@Override
	public Fragment getItem(int i) {
		return mlist.get(i);
	}
}