package com.zdx.youpai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zdx.youpai.R;
import com.zdx.youpai.adapter.MainFragmentPagerAdapter;
import com.zdx.youpai.ui.picture.PictureFragment;
import com.zdx.youpai.ui.shared.SharedFragment;
import com.zdx.youpai.ui.vedio.VideoFragment;
import com.zdx.youpai.view.AddPopupWindow;
import com.nineoldandroids.view.ViewHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;

/*
 * 作者：dongxu
 * 描述：主活动
 * 时间：2016/1/24
 * 版本：1.0
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final int TAB_HOME = 0;
    public static final int TAB_PICTURE = 1;
    public static final int TAB_MOVE = 2;

    private ViewPager mViewPager;
    private ArrayList<Fragment> fragmentList;
    private ImageView add, more;
    /**
     * 底部四个按钮
     */
    private LinearLayout mTabBtnHome;
    private LinearLayout mTabBtnPicture;
    private LinearLayout mTabBtnVideo;
    /*顶部自定义TopBar的home键*/
    private LinearLayout topHome;
    /*弹出框*/
    AddPopupWindow menuWindow;
    /*抽屉效果*/
    private DrawerLayout mDrawerLayout;
    //private SlidingMenu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvents();
        InitViewPager();

    }

    private void initView() {
        mTabBtnHome = (LinearLayout) findViewById(R.id.id_tab_bottom_home);
        mTabBtnPicture = (LinearLayout) findViewById(R.id.id_tab_bottom_picture);
        mTabBtnVideo = (LinearLayout) findViewById(R.id.id_tab_bottom_video);

        topHome = (LinearLayout) findViewById(R.id.top_bar_home);
        add = (ImageView) findViewById(R.id.top_bar_add);
        more = (ImageView) findViewById(R.id.top_bar_more);

        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawerLayout);
        //mMenu = (SlidingMenu) findViewById(R.id.id_menu);

        /*右侧菜单需要点击才能出现  意思是只有编程才能将其弹出。*/
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        /*设置Drawe的响应区块范围*/
        setDrawerLeftEdgeSize(MainActivity.this, mDrawerLayout, 0.1f);
    }

    /*
   * 初始化ViewPager
   */
    public void InitViewPager() {
        fragmentList = new ArrayList<Fragment>();
        Fragment sharedFragment = new SharedFragment();
        Fragment pictureFragment = new PictureFragment();
        Fragment videoFragment = new VideoFragment();
        fragmentList.add(sharedFragment);
        fragmentList.add(pictureFragment);
        fragmentList.add(videoFragment);

        mViewPager.setAdapter(new MainFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mViewPager.setCurrentItem(0);//初始设置当前显示标签页为第一页
        //设置缓存的Fragment数目为3，避免滑动时重复实例化
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());//页面变化时的监听器
    }

    private void initEvents() {
        mTabBtnHome.setOnClickListener(new tabListener(TAB_HOME));
        mTabBtnPicture.setOnClickListener(new tabListener(TAB_PICTURE));
        mTabBtnVideo.setOnClickListener(new tabListener(TAB_MOVE));

        add.setOnClickListener(this);
        more.setOnClickListener(this);
        topHome.setOnClickListener(this);

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = mDrawerLayout.getChildAt(0);
                View mMenu = drawerView;
                /*slideOffset相当于菜单显示在屏幕上的比例，实时变化*/
                float scale = 1 - slideOffset;
                /*左菜单变化0.7-1.0*/
                float leftScale = 1 - 0.3f * scale;
                /*右菜单变化1.0-0.8*/
                float rightScale = 0.8f + scale * 0.2f;

                if (drawerView.getTag().equals("LEFT")) {
                    /*设置左菜单XY方向上缩放比例*/
                    ViewHelper.setScaleX(mMenu, leftScale);
                    ViewHelper.setScaleY(mMenu, leftScale);
                    /*设置左菜单的透明度变化0.3-1.0*/
                    ViewHelper.setAlpha(mMenu, 0.4f + 0.6f * (1 - scale));
                    /*设置显示内容移动距离为左菜单宽度*/
                    ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * (1 - scale));
//                    ViewHelper.setPivotX(mContent, 0);
//                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    /*设置内容区域无效*/
                    mContent.invalidate();
                    ViewHelper.setScaleX(mContent, rightScale);
                    ViewHelper.setScaleY(mContent, rightScale);
                } else {
                    ViewHelper.setTranslationX(mContent, -mMenu.getMeasuredWidth() * slideOffset);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                /*最后在onDrawerClosed回调中，继续设置右侧滑菜单不可滑动出现*/
                mDrawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }
        });
    }

    /*设置左侧滑菜单感应宽度*/
    public static void setDrawerLeftEdgeSize(MainActivity activity,
                                             DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null)
            return;
        try {
            Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (dm.widthPixels * displayWidthPercentage)));
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.top_bar_add:
                uploadImage(MainActivity.this);
                break;
            case R.id.top_bar_more:
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT) == false) {
                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                /*当按钮点击切换到右菜单后，设置右菜单为可右滑隐藏*/
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                } else
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                break;
            case R.id.top_bar_home:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT) == false)
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                else
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                break;
            default:
                break;
        }
    }

    public class tabListener implements View.OnClickListener {
        private int index = 0;

        public tabListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mViewPager.setCurrentItem(index);
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int currentIndex;

        @Override
        public void onPageSelected(int position) {
            resetTabBtn();
            switch (position) {
                case TAB_HOME:
                    mTabBtnHome.findViewById(R.id.share)
                            .setBackgroundResource(R.drawable.icon_share_down);
                    break;
                case TAB_PICTURE:
                    mTabBtnPicture.findViewById(R.id.picture)
                            .setBackgroundResource(R.drawable.icon_picture_down);
                    break;
                case TAB_MOVE:
                    mTabBtnVideo.findViewById(R.id.video)
                            .setBackgroundResource(R.drawable.icon_video_down);
                    break;
            }
            currentIndex = position;
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }
    }

    /*如从fragment中打开需重写第一个Fragment自定义借口方法打开左右菜单*/
    public void onOpenMenu(View v) {
        switch (v.getId()) {
            case R.id.top_bar_add:
                mDrawerLayout.openDrawer(Gravity.LEFT);
                //mMenu.toggle();
                break;
            case R.id.top_bar_more:
                mDrawerLayout.openDrawer(Gravity.RIGHT);
                /*当按钮点击切换到右菜单后，设置右菜单为可右滑隐藏*/
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                break;
            default:
                break;
        }
    }

    public void uploadImage(final Activity context) {
        menuWindow = new AddPopupWindow(context);
        //显示在指定位置
        //menuWindow.showAtLocation(findViewById(R.id.id_top_bar), Gravity.NO_GRAVITY, 0,0);
        menuWindow.showAsDropDown(findViewById(R.id.id_action_bar_drop));
    }


    /*初始化底部按钮*/
    protected void resetTabBtn() {
        mTabBtnHome.findViewById(R.id.share)
                .setBackgroundResource(R.drawable.icon_share_up);
        mTabBtnPicture.findViewById(R.id.picture)
                .setBackgroundResource(R.drawable.icon_picture_up);
        mTabBtnVideo.findViewById(R.id.video)
                .setBackgroundResource(R.drawable.icon_video_up);
    }

}


