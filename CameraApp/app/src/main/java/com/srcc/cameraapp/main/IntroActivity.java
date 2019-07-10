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

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.srcc.cameraapp.R;

public class IntroActivity extends AppIntro2 {

    private static final String TAG = "INTRO";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int color = getColor(R.color.orange_intro);

        Log.i(TAG, "Start Intro Activity");

        /*
        * "Das Android-Roboter-Logo wurde aus einer von Google erstellten und
        * geteilten Arbeit reproduziert oder geändert und wird gemäß den Bedingungen
        * der Creative Commons 3.0-Lizenz für die Namensnennung verwendet."
        * */

        addSlide(IntroSlide.newInstance(R.layout.intro_welcome));
        addSlide(IntroSlide.newInstance(R.layout.intro_camera));
        addSlide(IntroSlide.newInstance(R.layout.intro_storage));
        addSlide(IntroSlide.newInstance(R.layout.intro_internet));
        addSlide(IntroSlide.newInstance(R.layout.intro_usage));

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
