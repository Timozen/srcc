package com.srcc.cameraapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.srcc.cameraapp.api.ApiService;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public static class ViewPagerAdapterBuilder {

        private ViewPager mViewpager;
        private FragmentManager mFragmentManager;
        private CompositeDisposable compositeDisposable;
        private Retrofit client;
        private ApiService apiService;

        public ViewPagerAdapterBuilder setmViewpager(ViewPager mViewpager) {
            this.mViewpager = mViewpager;
            return this;
        }

        public ViewPagerAdapterBuilder setmFragmentManager(FragmentManager mFragmentManager) {
            this.mFragmentManager = mFragmentManager;
            return this;
        }

        public ViewPagerAdapterBuilder setCompositeDisposable(CompositeDisposable compositeDisposable) {
            this.compositeDisposable = compositeDisposable;
            return this;
        }

        public ViewPagerAdapterBuilder setClient(Retrofit client) {
            this.client = client;
            return this;
        }

        public ViewPagerAdapterBuilder setApiService(ApiService apiService) {
            this.apiService = apiService;
            return this;
        }

        public ViewPagerAdapter createViewPagerAdapter() {
            return new ViewPagerAdapter(mViewpager, mFragmentManager, compositeDisposable, client, apiService);
        }
    }

    private ViewPager mViewpager;
    private FragmentManager mFragmentManager;
    private CompositeDisposable compositeDisposable;
    private Retrofit client;
    private ApiService apiService;

    public ViewPagerAdapter(ViewPager mViewpager, FragmentManager mFragmentManager, CompositeDisposable compositeDisposable, Retrofit client, ApiService apiService) {
        super(mFragmentManager);
        this.mViewpager = mViewpager;
        this.mFragmentManager = mFragmentManager;
        this.compositeDisposable = compositeDisposable;
        this.client = client;
        this.apiService = apiService;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return new CameraFragment.Builder()
                        .setCompositeDisposable(compositeDisposable)
                        .setmApiConnection(apiService)
                        .setmClient(client)
                        .createCameraFragment();
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
