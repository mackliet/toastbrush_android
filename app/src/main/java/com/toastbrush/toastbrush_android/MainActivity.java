package com.toastbrush.toastbrush_android;

import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity
        extends
        AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        BrowseImageFragment.OnFragmentInteractionListener,
        CreateImageFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener
{

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private int mNavItemId;
    private Fragment mCreateImageFragment;
    private Fragment mBrowseImageFragment;
    private Fragment mSettingsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);



        if(savedInstanceState == null)
        {
            mNavItemId = R.id.browse_button;
            // TODO get saved state stuff
        }
        else
        {
            mNavItemId = savedInstanceState.getInt("mNavItemId");
        }

        // Add the fragments to the layout
        Fragment frag = null;
        try {
            mBrowseImageFragment = (Fragment) BrowseImageFragment.class.newInstance();
            mCreateImageFragment = (Fragment) CreateImageFragment.class.newInstance();
            mSettingsFragment = (Fragment) SettingsFragment.class.newInstance();
            switch(mNavItemId)
            {
                case R.id.browse_button:
                default:
                    frag = mBrowseImageFragment;
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
            public void onClick(View v) {
                ((TextView)mNavigationView.getHeaderView(0).findViewById(R.id.account_text)).setText("PICTURE CLICKED");
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
// update highlighted item in the navigation menu
        menuItem.setChecked(true);
        switch (menuItem.getItemId()) {
            case R.id.browse_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mBrowseImageFragment).commit();
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

    public void onButtonPress(View view)
    {
        ((CreateImageFragment) mCreateImageFragment).onButtonPress(view);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
