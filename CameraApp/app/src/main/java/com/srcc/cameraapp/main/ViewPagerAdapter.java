package com.srcc.cameraapp.main;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.camera.CameraFragment;
import com.srcc.cameraapp.gallery.GalleryFragment;
import com.srcc.cameraapp.settings.SettingsFragment;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;

/**
 * This class will allow us to swipe between the 3 fragments we will have.
 * Order is left: settings, middle:camera, right:images
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    /**
     * Use the builder pattern for making the code more readable
     */
    static class Builder {

        private ViewPager viewPager;
        private FragmentManager fragmentManager;
        private CompositeDisposable compositeDisposable;
        private Retrofit client;
        private ApiService apiService;
        private Context context;

        Builder setViewPager(ViewPager viewPager) {
            this.viewPager = viewPager;
            return this;
        }

        Builder setFragmentManager(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            return this;
        }

        Builder setCompositeDisposable(CompositeDisposable compositeDisposable) {
            this.compositeDisposable = compositeDisposable;
            return this;
        }

        Builder setClient(Retrofit client) {
            this.client = client;
            return this;
        }

        Builder setApiService(ApiService apiService) {
            this.apiService = apiService;
            return this;
        }

        ViewPagerAdapter createViewPagerAdapter() {
            return new ViewPagerAdapter(viewPager, fragmentManager, compositeDisposable, client, apiService, context);
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }
    }

    private ViewPager viewPager;
    private FragmentManager fragmentManager;
    private final CompositeDisposable compositeDisposable;
    private Retrofit client;
    private ApiService apiService;
    private Context context;

    private ViewPagerAdapter(ViewPager viewPager, FragmentManager fragmentManager, CompositeDisposable compositeDisposable, Retrofit client, ApiService apiService, Context context) {
        super(fragmentManager);
        this.viewPager = viewPager;
        this.fragmentManager = fragmentManager;
        this.compositeDisposable = compositeDisposable;
        this.client = client;
        this.apiService = apiService;
        this.context = context;
    }

    /**
     * Give each fragment we want to show a position.
     * Keep in mind, the current i and i-1 and i+1 will
     * be loaded in the background.
     * Order will be left: settings, middle:camera, right:images
     *
     * @param i the current focused item
     * @return which fragment will be activated
     */
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new SettingsFragment.Builder()
                        .setApiService(apiService)
                        .setClient(client)
                        .createSettingsFragment();
            case 1:
                //Build the camera fragment with connection to the api
                return new CameraFragment.Builder()
                        .setCompositeDisposable(compositeDisposable)
                        .setApiService(apiService)
                        .setClient(client)
                        .createCameraFragment();
            case 2:
                return new GalleryFragment.Builder()
                        .setCompositeDisposable(compositeDisposable)
                        .setmApiConnection(apiService)
                        .createGalleryFragment();
        }
        return null;
    }

    /**
     * How many fragments we have, should always be 2 (or later 3)
     *
     * @return the amount of fragments
     */
    @Override
    public int getCount() {
        return 3;
    }
}
