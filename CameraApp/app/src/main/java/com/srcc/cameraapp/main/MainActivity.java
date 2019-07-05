package com.srcc.cameraapp.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.other.Utils;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Entry point of the app
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SRCC_CAMERA_MAIN";

    private CompositeDisposable compositeDisposable;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (sp.getBoolean("firstStart", true)) {
            Log.i(TAG, "App first start");
            Intent intent = new Intent(MainActivity.this, IntroActivity.class);
            startActivityForResult(intent, 1);
            Log.i(TAG, "Should have started Intro");
        } else {
            init();
        }

    }

    private void init(){
        if(viewPager == null) {
            //trashcan if somehow the connection is interrupted
            compositeDisposable = new CompositeDisposable();

            //http logging
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(loggingInterceptor);

            //create async client to our server (currently only local network)
            Retrofit mClient = new Retrofit.Builder()
                    .baseUrl("http://192.168.178.44:5000/")
//                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            Log.i(TAG, "Connect to server...");
            //attach the api requests to it
            ApiService mApiConnection = mClient.create(ApiService.class);

            Utils.setDebug(true);
            viewPager = findViewById(R.id.view_pager);

            //create the fragment view for nice swiping
            ViewPagerAdapter vpa = new ViewPagerAdapter.Builder()
                    .setFragmentManager(getSupportFragmentManager())
                    .setViewPager(viewPager)
                    .setClient(mClient)
                    .setApiService(mApiConnection)
                    .setCompositeDisposable(compositeDisposable)
                    .createViewPagerAdapter();

            viewPager.setAdapter(vpa);
            //set the starting page to the camera, currently on frame == 1
            viewPager.setCurrentItem(1);
        }
    }


    /**
     * We use this function for removing the android navigation
     * on bottom and top
     *
     * @param hasFocus check if our app has the focus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();

        if (hasFocus) {
            //set the tags for making it fullscreen
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private boolean firstResume = true;
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "MainActivity is resumed");
        if(!firstResume) {
            init();
        }else{
            firstResume = false;
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        Log.i(TAG, "Reenter called");
    }

    @Override
    protected void onDestroy() {
        //remove our not yet responded request
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
}
