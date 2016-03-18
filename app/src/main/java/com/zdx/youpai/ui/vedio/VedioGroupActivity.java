package com.zdx.youpai.ui.vedio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zdx.youpai.R;
import com.zdx.youpai.ui.vedio.adapter.VedioGroupAdapter;

import java.util.ArrayList;
import java.util.List;

public class VedioGroupActivity extends Activity implements View.OnClickListener{
    private GridView mGridView;
    private LinearLayout liner_back;
    private LinearLayout liner_share;

    private List<String> list;
    private String folderName;
    private TextView tv_date;
    private VedioGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vediogroup);

        initView();

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent mIntent = new Intent(VedioGroupActivity.this, VedioItemActivity.class);
                mIntent.putExtra("pos",position);
                mIntent.putStringArrayListExtra("allItem", (ArrayList<String>) list);
                startActivity(mIntent);
            }
        });
    }

    private void initView(){
        mGridView = (GridView) findViewById(R.id.child_grid);
        liner_back = (LinearLayout) findViewById(R.id.top_back);
        liner_share = (LinearLayout) findViewById(R.id.top_share);
        tv_date = (TextView) findViewById(R.id.top_date);

        liner_back.setOnClickListener(this);
        liner_share.setOnClickListener(this);

        list = getIntent().getStringArrayListExtra("data");
        folderName = getIntent().getExtras().getString("folderName");
        tv_date.setText(folderName);
        adapter = new VedioGroupAdapter(this, list, mGridView);
        mGridView.setAdapter(adapter);
    }
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "选中 " + adapter.getSelectItems().size() + " item", Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.top_back:
                VedioGroupActivity.this.finish();
                break;
            case R.id.top_share:
                break;
            default:
                break;
        }
    }
}

