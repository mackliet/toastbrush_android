package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.toastbrush.ToastbrushApplication;

import static com.toastbrush.ToastbrushApplication.getGoogleAccount;
import static com.toastbrush.ToastbrushApplication.getSignInClient;


public class MainActivity
        extends
        AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        BrowseImageFragment.OnFragmentInteractionListener,
        OnlineBrowseFragment.OnFragmentInteractionListener,
        CreateImageFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener
{

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private int mNavItemId;
    private Fragment mCreateImageFragment;
    private Fragment mBrowseImageFragment;
    private Fragment mOnlineBrowseFragment;
    private Fragment mSettingsFragment;
    private static final int RC_SIGN_IN = 1234;
    private AlertDialog.Builder mAlertBuilder;
    private TextView mAccountText;
    private ImageView mAccountImage;

    public void openImageInCreateImageFragment(String pkgedToastInfo)
    {
        Bundle args = new Bundle();
        args.putString("Image_data", pkgedToastInfo);
        mCreateImageFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, mCreateImageFragment)
                .addToBackStack(null)
                .commit();
        mNavItemId = R.id.create_button;
        mNavigationView.getMenu().findItem(mNavItemId).setChecked(true);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initializing a new alert dialog
        setContentView(R.layout.activity_main);

        mAlertBuilder = new AlertDialog.Builder(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);



        if(savedInstanceState == null)
        {
            mNavItemId = R.id.create_button;
        }
        else
        {
            mNavItemId = savedInstanceState.getInt("mNavItemId");
        }
        ((NavigationView)findViewById(R.id.nav_view)).getMenu().findItem(mNavItemId).setChecked(true);
        // Add the fragments to the layout
        Fragment frag = null;
        try {
            mBrowseImageFragment = BrowseImageFragment.class.newInstance();
            mOnlineBrowseFragment = OnlineBrowseFragment.class.newInstance();
            mCreateImageFragment = CreateImageFragment.class.newInstance();
            mSettingsFragment = SettingsFragment.class.newInstance();

            switch(mNavItemId)
            {
                case R.id.browse_button:
                default:
                    frag = mBrowseImageFragment;
                    break;
                case R.id.browse_online_button:
                    frag = mOnlineBrowseFragment;
                    break;
                case R.id.create_button:
                    frag = mCreateImageFragment;
                    break;
                case R.id.settings_button:
                    frag = mSettingsFragment;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, frag).commit();

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.bringToFront();
        mNavigationView.setNavigationItemSelectedListener(this);
        setupAccountSwitching();
        View navHeader = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);
        mAccountImage = (ImageView)navHeader.findViewById(R.id.account_picture);
        mAccountText = (TextView)navHeader.findViewById(R.id.account_text);
        if(wasSignedIn())
        {
            setupAccountGUI(getGoogleAccount());
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outstate)
    {
        super.onSaveInstanceState(outstate);
        outstate.putInt("mNavItemId", mNavItemId);
    }

    public void setupAccountSwitching() {
        ImageView imgView = mNavigationView.getHeaderView(0).findViewById(R.id.account_picture);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(wasSignedIn())
                {
                    mAlertBuilder.setTitle("Logout of Google Account");
                    mAlertBuilder.setMessage("Would you like to logout?");
                    mAlertBuilder.setPositiveButton("Logout", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getSignInClient().signOut();
                            mAccountImage.setImageResource(R.drawable.account_circle);
                            mAccountText.setText("\nNot Signed In");

                        }
                    });

                    mAlertBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                        }
                    });
                    mAlertBuilder.show();

                }
                else
                {
                    signIn();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }


    private void signIn()
    {
        Intent signInIntent = getSignInClient().getSignInIntent();
        signInIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        overridePendingTransition(0, 0);
    }

    private boolean wasSignedIn() {
        return getGoogleAccount() != null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if(handleSignInResult(task))
            {
                Toast.makeText(ToastbrushApplication.getAppContext(), "Successfully signed in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            setupAccountGUI(account);
            return true;
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TESTING", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(ToastbrushApplication.getAppContext(), "Failed to sign in", Toast.LENGTH_SHORT).show();

        }
        ToastbrushWebAPI.addUser(getGoogleAccount().getEmail(), getGoogleAccount().getEmail(), "", "", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("TESTING", response);
            }
        });
        return false;
    }

    private void setupAccountGUI(GoogleSignInAccount account) {
        // Signed in successfully, show authenticated UI.
        mAccountText.setText(account.getDisplayName() + "\n" + account.getEmail());
        final Uri personPhoto = account.getPhotoUrl();
        if(personPhoto != null)
        {
            ImageRequest imageRequest = new ImageRequest(personPhoto.toString(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    mAccountImage.setImageBitmap(getCroppedBitmap(response));
                }
            },1000, 1000, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("TESTING", error.getMessage());
                }
            });
            ToastbrushApplication.getRequestQueue().add(imageRequest);
        }
    }

    // Got this here
    // https://stackoverflow.com/questions/11932805/cropping-circular-area-from-bitmap-in-android
    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
// update highlighted item in the navigation menu
        menuItem.setChecked(true);
        switch (menuItem.getItemId()) {
            case R.id.browse_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mBrowseImageFragment).commit();
                break;
            case R.id.browse_online_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mOnlineBrowseFragment).commit();
                break;
            case R.id.create_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mCreateImageFragment).commit();
                break;
            case R.id.settings_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mSettingsFragment).commit();
                break;
            default:
                break;
        }
        mNavItemId = menuItem.getItemId();
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
