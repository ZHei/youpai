package com.zdx.youpai.test;

import android.content.Context;
import android.util.Log;


import com.zdx.youpai.utils.YouPaiJSONSerializer;

import java.util.ArrayList;
import java.util.UUID;

public class YouPaiLab {
    private static final String TAG = "YouPaiLab";
    private static final String FILENAME = "crimes.json";

    private ArrayList<YouPai> mYouPais;
    private YouPaiJSONSerializer mSerializer;

    private static YouPaiLab sYouPaiLab;
    private Context mAppContext;

    private YouPaiLab(Context appcontext) {
        mAppContext = appcontext;
        mSerializer = new YouPaiJSONSerializer(mAppContext, FILENAME);
        try {
            mYouPais = mSerializer.loadCrimes();
            Log.d(TAG,"初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
            mYouPais = new ArrayList<YouPai>();
            Log.e(TAG,"初始化失败", e);
        }

    }

    public void addCrime(YouPai c) {
        mYouPais.add(c);
    }

    public void deleteCrime(YouPai c){
        mYouPais.remove(c);
    }

    public boolean saveCrimes() {
        try {
            mSerializer.SaveCrimes(mYouPais);
            Log.d(TAG, "保存成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "保存失败", e);
            return false;
        }
    }

    /*通过get方法先判断是否已经存在了CrimLab，再实例化*/
    public static YouPaiLab get(Context c) {
        if (sYouPaiLab == null) {
            sYouPaiLab = new YouPaiLab(c.getApplicationContext());//不确定是否存在context，所以先获取
        }
        return sYouPaiLab;
    }

    public ArrayList<YouPai> getCrimes() {
        return mYouPais;
    }

    public YouPai getCrime(UUID id) {
        for (YouPai c : mYouPais) {
            if (c.getId().equals(id))
                return c;
        }
        return null;
    }
}
