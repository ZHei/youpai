/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zdx.youpai.ui.vedio;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.zdx.youpai.R;
import com.zdx.youpai.view.HackyViewPager;

import java.util.List;

import uk.co.senab.photoview.PhotoView;

public class VedioItemActivity extends Activity {

    private ViewPager mViewPager;
    private LinearLayout liner_back;
    public List<String> mlist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedioitem);

        liner_back = (LinearLayout)findViewById(R.id.top_back);

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        mlist = getIntent().getStringArrayListExtra("allItem");
        liner_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VedioItemActivity.this.finish();
            }
        });
        mViewPager.setAdapter(new SamplePagerAdapter());
        //定制预加载相邻页面的数目
        mViewPager.setOffscreenPageLimit(3);
        //设置点击进入当前图片
        mViewPager.setCurrentItem(getIntent().getExtras().getInt("pos"));
    }

    private class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());

            Bitmap bitmap = BitmapFactory.decodeFile(mlist.get(position));

            photoView.setImageBitmap(bitmap);
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
