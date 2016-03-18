package com.zdx.youpai.test;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class YouPai {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private DateFormat mDateFormat;
    private boolean mSolved;

    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_SOLVED = "solved";
    private static final String JSON_DATE = "date";

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getDateString() {
        return mDateFormat.format(mDate).toString();
    }

    public Date getDate() {
        return mDate;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    @Override
    public String toString() {
        return mTitle;
    }
/*新增使用的构造函数*/
    public YouPai() {
        mId = UUID.randomUUID();
        mDate = new Date();
        mDateFormat = new SimpleDateFormat("yyyy年MM月dd日 E HH时mm分");
        //mDateFormat.setTimeZone(TimeZone.getTimeZone(""));//设置时区
    }
/*读取使用的构造函数*/
    public YouPai(JSONObject json) throws JSONException{
        mId = UUID.fromString(json.getString(JSON_ID));
        if(json.has(JSON_TITLE)){
            mTitle = json.getString(JSON_TITLE);
        }
        mSolved = json.getBoolean(JSON_SOLVED);
        mDate = new Date(json.getLong(JSON_DATE));
        mDateFormat = new SimpleDateFormat("yyyy年MM月dd日 E HH时mm分");
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID,mId.toString());
        json.put(JSON_TITLE,mTitle);
        json.put(JSON_SOLVED,mSolved);
        json.put(JSON_DATE,mDate.getTime());
        return json;
    }

}
