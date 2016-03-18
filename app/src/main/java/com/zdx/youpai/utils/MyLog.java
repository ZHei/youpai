package com.zdx.youpai.utils;

/**
 * 文件名：com.example.dongxu.utils
 * 作者：dongxu
 * 时间：2016/1/27.
 */
public class MyLog {

    private static boolean gIsLog;
    protected static final String TAG = "YouPaiApplication";

    public MyLog() {
    }

    public static void setLog(boolean isLog) {
        gIsLog = isLog;
    }

    public static boolean getIsLog() {
        return gIsLog;
    }

    public static void v(String msg) {
        if(gIsLog) {
            android.util.Log.v(TAG, buildMessage(msg));
        }
    }

    public static void v(String tag, String msg) {
        if(gIsLog) {
            android.util.Log.v(tag, buildMessage(msg));
        }
    }

    public static void v(String tag, String msg, Throwable thr) {
        if(gIsLog) {
            android.util.Log.v(tag, buildMessage(msg), thr);
        }
    }
    public static void d(String msg) {
        if(gIsLog) {
            android.util.Log.d(TAG, buildMessage(msg));
        }
    }

    public static void d(String tag, String msg) {
        if(gIsLog) {
            android.util.Log.d(tag, buildMessage(msg));
        }
    }

    public static void d(String tag, String msg, Throwable thr) {
        if(gIsLog) {
            android.util.Log.d(tag, buildMessage(msg), thr);
        }
    }

    public static void i(String msg) {
        if(gIsLog) {
            android.util.Log.i(TAG, buildMessage(msg));
        }
    }

    public static void i(String tag, String msg) {
        if(gIsLog) {
            android.util.Log.i(tag, buildMessage(msg));
        }
    }

    public static void i(String tag, String msg, Throwable thr) {
        if(gIsLog) {
            android.util.Log.i(tag, buildMessage(msg), thr);
        }
    }

    public static void e(String msg) {
        if(gIsLog) {
            android.util.Log.e(TAG, buildMessage(msg));
        }
    }

    public static void e(String tag, String msg) {
        if(gIsLog) {
            android.util.Log.e(tag, buildMessage(msg));
        }
    }

    public static void e(String tag, String msg, Throwable thr) {
        if(gIsLog) {
            android.util.Log.e(tag, buildMessage(msg), thr);
        }
    }

    protected static String buildMessage(String msg) {
        StackTraceElement caller = new Throwable().fillInStackTrace().getStackTrace()[2];

        return new StringBuilder()
                .append(caller.getClassName())
                .append(".")
                .append(caller.getMethodName())
                .append("(): ")
                .append(msg).toString();
    }
}
