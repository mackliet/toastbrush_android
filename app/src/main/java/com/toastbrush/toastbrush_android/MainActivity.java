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


public class MainActivity
        extends
        AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        AndrewFragment.OnFragmentInteractionListener,
        MichaelFragment.OnFragmentInteractionListener
{

    private DrawerLayout mDrawerLayout;
    private int mNavItemId;
    private Fragment michaelFragment;
    private Fragment andrewFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        // Add the fragments to the layout
        if(savedInstanceState == null)
        {
            try {
                andrewFragment = (Fragment) AndrewFragment.class.newInstance();
                michaelFragment = (Fragment) MichaelFragment.class.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, andrewFragment).commit();
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
// update highlighted item in the navigation menu
        menuItem.setChecked(true);
        Fragment frag = null;
        Class frag_class = null;
        switch (menuItem.getItemId()) {
            case R.id.andrew_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, andrewFragment).commit();
                break;
            case R.id.michael_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, michaelFragment).commit();                break;
            default:
                break;
        }
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
        ((MichaelFragment)michaelFragment).onButtonPress(view);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
