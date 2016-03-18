package com.zdx.youpai.ui.shared;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zdx.youpai.ui.shared.adapter.SharedListAdapter;
import com.zdx.youpai.test.YouPai;
import com.zdx.youpai.test.YouPaiLab;
import com.zdx.youpai.test.ActivityYouPaiPager;
import com.zdx.youpai.R;
import com.zdx.youpai.test.YouPaiFragment;
import com.zdx.youpai.ui.BaseFragment;

import java.util.ArrayList;

public class SharedFragment extends BaseFragment {

    private ListView mListView;
    private ArrayList<YouPai> mYouPais;
    private SharedListAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mYouPais = YouPaiLab.get(getActivity()).getCrimes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_share, parent, false);
        mListView = (ListView) v.findViewById(R.id.id_main_listview);
        adapter = new SharedListAdapter(getActivity(), mYouPais);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                YouPai c = adapter.getItem(position);
                Intent intent = new Intent(getActivity(), ActivityYouPaiPager.class);
                intent.putExtra(YouPaiFragment.EXTRA_CRIME_ID, c.getId());
                startActivityForResult(intent, 0);
            }
        });

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            registerForContextMenu(mListView);
        }else{
            /*多选模式*/
            mListView.setChoiceMode(mListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle("选择删除");
                    MenuInflater menuInflater = mode.getMenuInflater();
                    menuInflater.inflate(R.menu.youpai_list_item_context,menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.contextmenu_item_delete :
                            for(int i = adapter.getCount() - 1; i >= 0;i--){
                                if(mListView.isItemChecked(i)){
                                    YouPaiLab.get(getActivity()).deleteCrime(adapter.getItem(i));
                                    YouPaiLab.get(getActivity()).saveCrimes();
                                }
                            }
                            mode.finish();
                            adapter.notifyDataSetChanged();
                            return true;
                        default:
                            return false;
                    }

                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });
        }
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.youpai_list_item_context,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        YouPai c = adapter.getItem(info.position);
        switch (item.getItemId()){
            case R.id.contextmenu_item_delete :
                YouPaiLab.get(getActivity()).deleteCrime(c);
                YouPaiLab.get(getActivity()).saveCrimes();
                adapter.notifyDataSetChanged();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}