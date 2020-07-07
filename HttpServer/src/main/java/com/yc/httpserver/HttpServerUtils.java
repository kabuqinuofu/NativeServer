package com.yc.httpserver;

import android.annotation.SuppressLint;
import android.content.Context;

public class HttpServerUtils {

    private Context context;

    @SuppressLint("StaticFieldLeak")
    private volatile static HttpServerUtils fUtils;

    private HttpServerUtils(Context context) {
        this.context = context;
    }

    public static void init(Context context) {
        if (fUtils == null) {
            synchronized (HttpServerUtils.class) {
                if (fUtils == null) {
                    fUtils = new HttpServerUtils(context);
                }
            }
        }
    }

    public static Context getAppContext() {
        if (fUtils != null) return fUtils.context.getApplicationContext();
        throw new NullPointerException("To initialize first");
    }

}