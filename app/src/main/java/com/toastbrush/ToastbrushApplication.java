package com.toastbrush;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ToastbrushApplication extends Application
{
    private static RequestQueue mRequestQueue;
    private static Context context;

    public void onCreate() {
        super.onCreate();
        ToastbrushApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return ToastbrushApplication.context;
    }

    public static RequestQueue getRequestQueue()
    {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(ToastbrushApplication.getAppContext());
        }
        return mRequestQueue;
    }
}
