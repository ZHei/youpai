package com.zdx.youpai.test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.zdx.youpai.R;

import java.util.ArrayList;
import java.util.UUID;

public class ActivityYouPaiPager extends FragmentActivity {
    private ViewPager mViewPager;
    private ArrayList<YouPai> mYouPais;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*此Activity暂时只有一个全屏显示的Viewp，用setId的方法*/
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewpager);
        setContentView(mViewPager);

        mYouPais = YouPaiLab.get(this).getCrimes();
        FragmentManager fm = getSupportFragmentManager();

        //fragment比较多用state，比较少用FragementAdapter
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {

            @Override
            public int getCount() {
                return mYouPais.size();
            }

            /*首先获取了数据集中指定位置的Crime实例，然后利用该
            Crime实例的ID创建并返回一个有效配置的CrimeFragment*/
            @Override
            public Fragment getItem(int position) {
                YouPai youPai = mYouPais.get(position);
                return YouPaiFragment.newInstance(youPai.getId());
            }
        });
        mViewPager.setOffscreenPageLimit(3);//定制预加载相邻页面的数目

        UUID mCrimeId = (UUID) getIntent().getSerializableExtra(YouPaiFragment.EXTRA_CRIME_ID);
        for(int i = 0 ; i< mYouPais.size() ; i++) {
            if(mYouPais.get(i).getId().equals(mCrimeId)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }
/*当前哪一个页面被选中只需实现onPageSelected(...) 方法即可。
onPageScrolled(...) 方法可告知我们页面将会滑向哪里，
而onPageScrollStateChanged(...) 方法可告知我们当前页面所处的行为状态，如正在被用户
滑动、页面滑动入位到完全静止以及页面切换完成后的闲置状态*/
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(mYouPais.get(position).getTitle() != null){
                    setTitle(mYouPais.get(position).getTitle());
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
