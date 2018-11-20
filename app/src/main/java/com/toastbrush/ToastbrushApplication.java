package com.toastbrush;

import android.app.Application;
import android.content.Context;
import android.support.v7.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.toastbrush.toastbrush_android.BLEGatt;
import com.toastbrush.toastbrush_android.R;

public class ToastbrushApplication extends Application
{
    private static RequestQueue mRequestQueue;
    private static BLEGatt mBluetoothServer;
    private static Context context;
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

    public static BLEGatt getBluetoothServer()
    {
        if (mBluetoothServer == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mBluetoothServer = new BLEGatt(context);
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
