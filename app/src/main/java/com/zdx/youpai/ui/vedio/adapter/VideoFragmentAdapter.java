package com.zdx.youpai.ui.vedio.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.zdx.youpai.data.ImageBean;
import com.zdx.youpai.R;
import com.zdx.youpai.utils.NativeImageLoader;
import com.zdx.youpai.view.MyImageView;

import java.util.List;


public class VideoFragmentAdapter extends BaseAdapter{
    private final String TAG = "VedioFragmentAdapter";
    private List<ImageBean> list;
    private Point mPoint = new Point(0, 0);//用来封装ImageView的宽和高的对象
    private PullToRefreshGridView mGridView;
    protected LayoutInflater mInflater;

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public VideoFragmentAdapter(Context context, List<ImageBean> list, PullToRefreshGridView mGridView){

        this.list = list;
        this.mGridView = mGridView;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        ImageBean mImageBean = list.get(position);
        String path = mImageBean.getTopImagePath();

        //使用convertView减少重复加载提高效率
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.video_group_item, null);
            viewHolder.mImageView = (MyImageView) convertView.findViewById(R.id.group_image);
            viewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.group_title);
            viewHolder.mTextViewCounts = (TextView) convertView.findViewById(R.id.group_count);

            //用来监听ImageView的宽和高
            viewHolder.mImageView.setOnMeasureListener(new MyImageView.OnMeasureListener() {

                @Override
                public void onMeasureSize(int width, int height) {
                    mPoint.set(width, height);
                    Log.d(TAG,mPoint.toString());
                }
            });

            convertView.setTag(viewHolder); //将ViewHolder存储在convertView
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTextViewTitle.setText(mImageBean.getFolderName().substring(5));
        viewHolder.mTextViewCounts.setText(Integer.toString(mImageBean.getImageCounts()));
        //给ImageView设置路径Tag,这是异步加载图片的小技巧
        viewHolder.mImageView.setTag(path);

        //利用NativeImageLoader类加载本地图片
        Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint, new NativeImageLoader.NativeImageCallBack() {

            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
                if(bitmap != null && mImageView != null){
                        mImageView.setImageBitmap(bitmap);
                }
            }
        });

        if(bitmap != null){
            viewHolder.mImageView.setImageBitmap(bitmap);
        }else{
            viewHolder.mImageView.setImageResource(R.drawable.pictures_no);
        }

        return convertView;
    }

    public static class ViewHolder{
        public MyImageView mImageView;
        public TextView mTextViewTitle;
        public TextView mTextViewCounts;
    }
}
