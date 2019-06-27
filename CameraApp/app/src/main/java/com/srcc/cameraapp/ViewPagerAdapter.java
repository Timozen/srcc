package com.srcc.cameraapp;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private ViewPager mViewpager;

    public ViewPagerAdapter(FragmentManager fm, ViewPager viewPager) {
        super(fm);
        mViewpager = viewPager;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return CameraFragment.newInstance();
            case 1:
                return ShowImagesFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
