package com.srcc.cameraapp.main;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.srcc.cameraapp.R;

public class IntroActivity extends AppIntro {

    private static final String TAG = "INTRO";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int color = getColor(R.color.orange_intro);

        Log.i(TAG, "Start Intro Activity");

        //Initial welcome page
        SliderPage welcomePage = new SliderPage();
        welcomePage.setTitle(getString(R.string.intro_welcome_title));
        welcomePage.setDescription(getString(R.string.intro_welcome_description));
        welcomePage.setImageDrawable(R.drawable.ic_mood_black_128dp);
        welcomePage.setBgColor(color);

        //Camera Permission
        SliderPage cameraPage = new SliderPage();
        cameraPage.setTitle(getString(R.string.intro_camera_title));
        cameraPage.setDescription(getString(R.string.intro_camera_description));
        cameraPage.setImageDrawable(R.drawable.ic_photo_camera_black_128dp);
        cameraPage.setBgColor(color);

        //External Store Permission
        SliderPage storagePage = new SliderPage();
        storagePage.setTitle(getString(R.string.intro_storage_title));
        storagePage.setDescription(getString(R.string.intro_storage_description));
        storagePage.setImageDrawable(R.drawable.ic_storage_black_128dp);
        storagePage.setBgColor(color);

        //Internet Permission
        SliderPage internetPage = new SliderPage();
        internetPage.setTitle(getString(R.string.intro_internet_title));
        internetPage.setDescription(getString(R.string.intro_internet_description));
        internetPage.setImageDrawable(R.drawable.ic_internet_black_128dp);
        internetPage.setBgColor(color);

        //Usage
        SliderPage usagePage = new SliderPage();
        usagePage.setTitle(getString(R.string.intro_usage_title));
        usagePage.setDescription(getString(R.string.intro_usage_description));
        usagePage.setImageDrawable(R.drawable.ic_swipe_arrows_black_128dp);
        usagePage.setBgColor(color);

        addSlide(AppIntroFragment.newInstance(welcomePage));
        addSlide(AppIntroFragment.newInstance(cameraPage));
        addSlide(AppIntroFragment.newInstance(storagePage));
        addSlide(AppIntroFragment.newInstance(internetPage));
        addSlide(AppIntroFragment.newInstance(usagePage));

        askForPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        askForPermissions(new String[]{Manifest.permission.INTERNET}, 4);

        showSkipButton(false);
        showStatusBar(false);
        setNavBarColor(R.color.orange_intro);
    }

    @Override
    public void onBackPressed() {}

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        getPrefs.edit().putBoolean("firstStart", false).apply();
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

}
