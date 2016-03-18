package com.zdx.youpai.ui.vedio;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.zdx.youpai.ui.BaseFragment;
import com.zdx.youpai.ui.vedio.adapter.VideoFragmentAdapter;
import com.zdx.youpai.data.ImageBean;
import com.zdx.youpai.R;
import com.zdx.youpai.ui.picture.PictureGroupActivity;
import com.zdx.youpai.view.DatePickerFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class VideoFragment extends BaseFragment implements View.OnClickListener {
    public static final int REQUEST_DATE = 0;
    public static final String DIALOG_DATE = "date";

    private SimpleDateFormat mDateFormat;
    private Date date;
    private String filterString;//筛选显示相册的字符串
    private TextView mSelectDate;
    private ImageView mBack, mNext;

    private FragmentManager fm;
    private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
    private List<ImageBean> mList = new ArrayList<ImageBean>();
    private ProgressDialog mProgressDialog;
    private VideoFragmentAdapter vidGroupAdapter;
    private PullToRefreshGridView mGroupGridView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        date = new Date();
        //时间格式用使用空格，用于计算时间切换时
        mDateFormat = new SimpleDateFormat("yyyy 年 M 月");
        filterString = mDateFormat.format(date).toString();

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        mSelectDate = (TextView) view.findViewById(R.id.pic_select_date);
        mBack = (ImageView) view.findViewById(R.id.pic_top_back);
        mNext = (ImageView) view.findViewById(R.id.pic_top_next);
        mGroupGridView = (PullToRefreshGridView) view.findViewById(R.id.picture_grid);

        initIndicator();
        //初始化时启用进度条
        mProgressDialog = ProgressDialog.show(getActivity(), null, "正在加载...");

        new GetImageTask().execute(filterString);
        mGroupGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                List<String> childList = mGruopMap.get(mList.get(position).getFolderName());

                String folderName = mList.get(position).getFolderName();
                Intent mIntent = new Intent(getActivity(), VedioGroupActivity.class);
                mIntent.putStringArrayListExtra("data", (ArrayList<String>) childList);
                mIntent.putExtra("folderName", folderName);
                startActivity(mIntent);
            }
        });
        mGroupGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
//                Log.e("TAG", "onPullDownToRefresh");
                String label = DateUtils.formatDateTime(
                        getActivity().getApplicationContext(),
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_YEAR);

                // 显示最后更新时间
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                new GetImageTask().execute(filterString);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
            }
        });

        mBack.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mSelectDate.setOnClickListener(this);
        mSelectDate.setText(filterString);
        return view;
    }

    private void initIndicator() {
        ILoadingLayout startLabels = mGroupGridView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("下拉刷新");// 刚下拉时，显示的提示
        startLabels.setReleaseLabel("松开刷新...");// 下来达到一定距离时，显示的提示
        startLabels.setRefreshingLabel("正在刷新...");// 刷新时

        //本地图片的展示不需要上拉加载
        ILoadingLayout endLabels = mGroupGridView.getLoadingLayoutProxy(false, true);
        endLabels.setPullLabel("上拉加载");// 刚下拉时，显示的提示
        endLabels.setReleaseLabel("释放加载...");// 下来达到一定距离时，显示的提示
        endLabels.setRefreshingLabel("正在加载...");// 刷新时
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.pic_select_date:
                fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dateDialog = DatePickerFragment.newInstance(date);
                dateDialog.setTargetFragment(VideoFragment.this, REQUEST_DATE);
                //string参数可唯一识别存放在FragmentManager队列中的DialogFragment
                dateDialog.show(fm, DIALOG_DATE);
                break;
            case R.id.pic_top_back:
                filterString = getMonthStartAndEnd(filterString,-1);
                mSelectDate.setText(filterString);

                new GetImageTask().execute(filterString);
                break;
            case R.id.pic_top_next:
                filterString = getMonthStartAndEnd(filterString,1);
                mSelectDate.setText(filterString);

                new GetImageTask().execute(filterString);
                break;
            default:
                break;
        }
    }

    /**
          * 所在日期前后偏移几个月的第一天和最后一天
          * @param date 所在日期
          * @param offset 偏移量（例如：当月0，下月1，上月-1）
          * @return 当月的第一天和最后一天
          */
    private static String getMonthStartAndEnd(String date, int offset) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy 年 M 月");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.valueOf(date.split(" ")[0]));
        cal.set(Calendar.MONTH, Integer.valueOf(date.split(" ")[2])-1);
//        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, offset);
//        cal.add(Calendar.DATE, -1);
        String end = sdf.format(cal.getTime());

//        cal.set(Calendar.DATE, 1);
//        String start = sdf.format(cal.getTime());
        return end;
    }

    private class GetImageTask extends AsyncTask<String, Void, String> {
        /**
         * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
         * 扫描的图片都存放在Cursor中，我们先要将图片按照文件夹进行分类,
         * 我们使用了HashMap来进行分类并将结果存储到mGruopMap（Key是文件夹名，Value是文件夹中的图片路径的List）中，
         */
        @Override
        protected String doInBackground(String... filterString) {
            //确认外部SD卡是否存在
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(getActivity(), "暂无外部存储", Toast.LENGTH_SHORT).show();
                return null;
            }
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            ContentResolver mContentResolver = getActivity().getContentResolver();
            //更新ContentResolver
            updateGallery();
            //只查询jpeg和png的图片,查询的图片排序为图片的修改时间顺序
            Cursor mCursor = mContentResolver.query(mImageUri, null,
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

            //先clear下mGruopMap,刷新时才不会重复加载
            mGruopMap.clear();

            mCursor.moveToLast();
            do{
                //获取图片的路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                //获取该图片的父路径名
                String parentparentName = new File(path).getParentFile().getParentFile().getName();
                String parentName = new File(path).getParentFile().getName();

                //根据父路径名将图片放入到mGruopMap中
                if (parentparentName.equals("VCamera")) {
                    Log.e("ha", path);
                    if (!mGruopMap.containsKey(parentName)) { //如果Map包含指定键的映射，则返回true；
                        List<String> chileList = new ArrayList<String>();

                        chileList.add(path);
//                        Log.e("ha", path);
//                        Log.e("ha", parentparentName);
//                        Log.e("ha", parentName);
//                        Log.e("ha", filterString[0]);
                        if (parentName.replace(" ", "").contains(filterString[0].replace(" ", ""))) {
                            mGruopMap.put(parentName, chileList);
                        }
                    }
                    else {
                        mGruopMap.get(parentName).add(path);
                    }
                }
            }while (mCursor.moveToPrevious());

            mCursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            vidGroupAdapter = new VideoFragmentAdapter(getActivity(), mList = subGroupOfImage(mGruopMap), mGroupGridView);
            mGroupGridView.setAdapter(vidGroupAdapter);
//            vidGroupAdapter.notifyDataSetChanged();
            //调用onRefreshComplete()完成刷新。
            mGroupGridView.onRefreshComplete();
            //关闭读取精度条，进入程序时起作用
            mProgressDialog.dismiss();
        }
    }
    /*每次查询前，应该发广播更新下
    * 这样可以实时更新mContentResolver
    * 在发广播处，可以指定path路径的uri，当然也可以传MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    * */
    private void updateGallery() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        String path = Environment.getExternalStorageDirectory() + "/Pictures/GPUImage";
//        Uri uri = Uri.fromFile(new File(path));
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getActivity().sendBroadcast(intent);
    }
    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     * 将mGruopMap的数据组装到List中，
     * 在List中存放GridView中的每个item的数据对象ImageBean, 遍历HashMap对象，之后就是给GridView设置Adapter。
     *
     * @param mGruopMap
     * @return
     */
    private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap) {
        List<ImageBean> list = new ArrayList<ImageBean>();
        if (mGruopMap.size() == 0) {
            return list;
        }

        //Iterator迭代，相当于用循环语句从一个库里取出数据
        Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            ImageBean mImageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();

            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));//获取该组的第一张图片及最新图片

            list.add(mImageBean);
        }
        //对list排序
        Comparator comp = new SortComparator();
        Collections.sort(list, comp);
        return list;
    }
    /*list按照ImageBean的文件名排序方法*/
    private class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            ImageBean a = (ImageBean) lhs;
            ImageBean b = (ImageBean) rhs;
            int aInt = Integer.valueOf(a.getFolderName().replace("年", "").replace("月", "").replace("日", ""));
            int bInt = Integer.valueOf(b.getFolderName().replace("年", "").replace("月", "").replace("日", ""));

            return ( bInt - aInt);
        }
    }
    /*
    * fragment能够从activity中接收返回结果，但其自身无法产生返回结果。
    * 只有activity拥有返回结果。因此尽管Fragment有自己的startActivityForResult(...)
    * 和onActivityResult(...) 方法，但却不具有任何setResult(...) 方法。
    * 相反，我们应通知托管activity返回结果值。
    */
    public void returnResult() {
        getActivity().setResult(Activity.RESULT_OK, null);//null是intent
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent idate) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_DATE) {
            date = (Date) idate.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            filterString = mDateFormat.format(date).toString();
            mSelectDate.setText(filterString);
            new GetImageTask().execute(filterString);
        }
    }
}
