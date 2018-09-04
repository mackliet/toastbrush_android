package com.toastbrush.toastbrush_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class BrowsePagerAdapter extends FragmentPagerAdapter {

    public BrowsePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null ;//todo
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt("NULL", i + 1); //todo
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}
