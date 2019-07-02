package com.srcc.cameraapp.main;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

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

        Log.i(TAG, "Start Intro Activity");

        //Initial welcome page
        SliderPage welcomePage = new SliderPage();
        welcomePage.setTitle("Welcome");
        welcomePage.setDescription("This app allows you to take camera pictures and those will quality will be improved.");
        welcomePage.setImageDrawable(R.drawable.ic_mood_black_128dp);
        welcomePage.setBgColor(getColor(R.color.orange));

        //Camera Permission
        SliderPage cameraPage = new SliderPage();
        cameraPage.setTitle("Camera");
        cameraPage.setDescription("Because we'd like to take pictures, we need the permission of using the camera.");
        cameraPage.setImageDrawable(R.drawable.ic_photo_camera_black_128dp);
        cameraPage.setBgColor(getColor(R.color.orange));

        //External Store Permission
        SliderPage storagePage = new SliderPage();
        storagePage.setTitle("Storage");
        storagePage.setDescription("The taken pictures and the improved pictures will be store on your phone. Therefore we need the storage permission.");
        storagePage.setImageDrawable(R.drawable.ic_storage_black_128dp);
        storagePage.setBgColor(getColor(R.color.orange));

        //Internet Permission
        SliderPage internetPage = new SliderPage();
        internetPage.setTitle("Internet");
        internetPage.setDescription("To create the improved pictures the app has to communicate with our server. Therefore we need to access the internet.");
        internetPage.setImageDrawable(R.drawable.ic_internet_black_128dp);
        internetPage.setBgColor(getColor(R.color.orange));

        //Usage
        SliderPage usagePage = new SliderPage();
        usagePage.setTitle("Usage");
        usagePage.setDescription("The main focus is the camera view. If you swipe to the left the settings will appear. Swiping to the right will show the taken pictures.");
        usagePage.setImageDrawable(R.drawable.ic_swipe_arrows_black_128dp);
        usagePage.setBgColor(getColor(R.color.orange));


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
        setNavBarColor(R.color.orange);

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //  Make a new preferences editor
        SharedPreferences.Editor e = getPrefs.edit();
        //  Edit preference to make it false because we don't want this to run again
        e.putBoolean("firstStart", false);
        //  Apply changes
        e.apply();

        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

}
