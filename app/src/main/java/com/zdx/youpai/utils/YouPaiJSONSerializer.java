package com.zdx.youpai.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.zdx.youpai.test.YouPai;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class YouPaiJSONSerializer {
    private Context mContext;
    private String mFileName;

    public YouPaiJSONSerializer(Context c, String s) {
        mContext = c;
        mFileName = s;
    }

    public void SaveCrimes(ArrayList<YouPai> youPais) throws IOException, JSONException {
        JSONArray array = new JSONArray();
        for (YouPai c : youPais) {
            array.put(c.toJSON());
        }

        Writer writer = null;
        try {
            OutputStream outputStream = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(outputStream);
            writer.write(array.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ArrayList<YouPai> loadCrimes() throws IOException, JSONException {
        ArrayList<YouPai> youPais = new ArrayList<YouPai>();
        BufferedReader reader = null;
        try {
            InputStream in = mContext.openFileInput(mFileName);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            JSONArray array = new JSONArray(jsonString.toString());
            Log.v("初始化加载的数据", jsonString.toString());
            for (int i = 0; i < array.length(); i++) {
                youPais.add(new YouPai(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return youPais;
    }
}
