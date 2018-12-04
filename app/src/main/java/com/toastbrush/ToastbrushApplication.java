package com.toastbrush;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.toastbrush.toastbrush_android.BLEGatt;

public class ToastbrushApplication extends Application
{
    private static RequestQueue mRequestQueue;
    private static BLEGatt mBluetoothServer;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static GoogleSignInClient mGoogleSignInClient;

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

    public static BLEGatt setupBluetoothServer(Activity activity)
    {
        return getBluetoothServer().enableBluetooth(activity);
    }

    public static BLEGatt getBluetoothServer()
    {
        if(mBluetoothServer == null)
        {
            mBluetoothServer = new BLEGatt();
        }
        return mBluetoothServer;
    }

    public static GoogleSignInClient getSignInClient()
    {
        if(mGoogleSignInClient == null)
        {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(ToastbrushApplication.getAppContext(), gso);
        }
        return mGoogleSignInClient;
    }

    public static GoogleSignInAccount getGoogleAccount()
    {
        return GoogleSignIn.getLastSignedInAccount(getAppContext());
    }
}
