package com.srcc.cameraapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

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
