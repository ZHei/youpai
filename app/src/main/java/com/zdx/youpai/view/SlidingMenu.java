//slidingmenu的方式展示左右侧滑栏的方式暂时不适用
package com.zdx.youpai.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.zdx.youpai.R;

/*  1.我们的自定义控件和其他的控件一样,应该写成一个类,而这个类的属性是是由自己来决定的.
    2.我们要在res/values目录下建立一个attrs.xml的文件,并在此文件中增加对控件的属性的定义.
    3.使用AttributeSet来完成控件类的构造函数,并在构造函数中将自定义控件类中变量与attrs.xml中的属性连接起来.
    4.在自定义控件类中使用这些已经连接的属性变量.
    5.将自定义的控件类定义到布局用的xml文件中去.
    6.在界面中生成此自定义控件类对象,并加以使用.*/
public class SlidingMenu extends HorizontalScrollView//一种横向滚动视图控件
{
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * dp
     */
    private int mLeftMenuRightPadding;
    private int mRightMenuLeftpadding;
    /**
     * 菜单的宽度
     */
    private int mLeftMenuWidth;
    private int mRightMenuWidth;
    private int mHalfLeftMenuWidth;
    private int mHalfRightMenuWidth;

    private boolean isOpen;

    private boolean once;
    public ViewGroup mLeftMenu;
    public ViewGroup mRightMenu;
    public ViewGroup mContent;

    public SlidingMenu(Context context) {
        this(context, null, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScreenWidth = DeviceUtils.getScreenWidth(context);
        //TypedArray表示自定义控件的属性项
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SlidingMenu, defStyle, 0);

        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SlidingMenu_rightPadding:
                    // 设置左边菜单框右内框
                    mLeftMenuRightPadding = a.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, 500f,
                                    getResources().getDisplayMetrics()));// 默认为10DP
                    break;
                case R.styleable.SlidingMenu_leftPadding:
                    // 设置右边菜单框左内框
                    mRightMenuLeftpadding = a.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, 500f,
                                    getResources().getDisplayMetrics()));// 默认为10DP
                    break;
            }
        }
        a.recycle();//循环
    }

    /*
    * 可以说重载onMeasure()，onLayout()，onDraw()三个函数构建了自定义View的外观形象。
    * 再加上onTouchEvent()等重载视图的行为，可以构建任何我们需要的可感知到的自定义View。
    * 1.View本身大小多少，这由onMeasure()决定；
    * 2.View在ViewGroup中的位置如何，这由onLayout()决定；
    * 3.绘制View，onDraw()定义了如何绘制这个View。
    */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 显示的设置一个宽度
         */
        if (!once) {
            LinearLayout wrapper = (LinearLayout) getChildAt(0);//获取菜单布局的第一个linearlayout
            mLeftMenu = (ViewGroup) wrapper.getChildAt(0);//左边菜单
            mContent = (ViewGroup) wrapper.getChildAt(1);//中间内容
            mRightMenu = (ViewGroup) wrapper.getChildAt(2);//右侧菜单
            mLeftMenuWidth = mScreenWidth - mLeftMenuRightPadding;
            mRightMenuWidth = mScreenWidth - mRightMenuLeftpadding;

            mHalfLeftMenuWidth = mLeftMenuWidth / 2;
            mHalfRightMenuWidth = mRightMenuWidth / 2;
            mLeftMenu.getLayoutParams().width = mLeftMenuWidth;
            mContent.getLayoutParams().width = mScreenWidth;
            mRightMenu.getLayoutParams().width = mRightMenuWidth;

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            // 将菜单隐藏
            this.scrollTo(mLeftMenuWidth, 0);//设置要滚动到的xy位置
            once = true;
        }
    }

    //触摸滑动的响应
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            // 滑动时已经拖动了，Up时，进行判断，如果未显示区域大于菜单宽度一半则完全显示，否则隐藏
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();//手机屏幕显示区域左上角x坐标减去ViewGroup视图左上角x坐标即滑动的距离（有正负）
                Log.v("scrollx", String.valueOf(scrollX).toString());

                if (scrollX < mHalfLeftMenuWidth && scrollX > 0) {
                    this.smoothScrollTo(0, 0);
                    isOpen = true;
                } else if (scrollX < mLeftMenuWidth && scrollX > mHalfLeftMenuWidth) {
                    this.smoothScrollTo(mLeftMenuWidth, 0);
                    isOpen = false;
                } else if (scrollX < mLeftMenuWidth + mHalfRightMenuWidth && scrollX > mLeftMenuWidth) {
                    this.smoothScrollTo(mLeftMenuWidth, 0);
                } else if (scrollX > mLeftMenuWidth + mHalfRightMenuWidth) {
                    this.smoothScrollTo(mLeftMenuWidth + mRightMenuWidth, 0);
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
/*我们在onScrollChanged里面，拿到 l 也就是个getScrollX，即菜单已经显示的宽度值*/
        float scale = l * 1.0f / mLeftMenuWidth;
        float leftScale = 1 - 0.3f * scale;
        float rightScale = 0.8f + scale * 0.2f;

        ViewHelper.setScaleX(mLeftMenu, leftScale);
        ViewHelper.setScaleY(mLeftMenu, leftScale);//从0.7-1.0缩放
        ViewHelper.setAlpha(mLeftMenu, 0.2f + 0.4f * (1 - scale));//透明度从0.6-1
        ViewHelper.setTranslationX(mLeftMenu, mLeftMenuWidth * scale * 0.4f);//设置左菜单被拉出时开始显示的距离

        ViewHelper.setPivotX(mContent, 0);
        ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);
        ViewHelper.setScaleX(mContent, rightScale);
        ViewHelper.setScaleY(mContent, rightScale);//从1.0-0.8缩放

    }

    /**
     * 打开菜单
     */
    public void openMenu() {
        if (isOpen)
            return;
        this.smoothScrollTo(0, 0);
        isOpen = true;
    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        if (isOpen) {
/*类似scrollBy(int, int)，但是呈现平滑滚动，而非瞬间滚动
参数
dx    要滚动的X轴像素差值（译者注：横向像素差值）
dy    要滚动的Y轴像素差值（译者注：纵向像素差值）
*/
            this.smoothScrollTo(mLeftMenuWidth, 0);
            isOpen = false;
        }
    }

    /**
     * 切换菜单状态
     */
    public void toggle() {
        if (isOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }
}

