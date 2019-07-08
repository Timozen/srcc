package com.srcc.cameraapp.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.gallery.GalleryFragment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Entry point of the app
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SRCC_CAMERA_MAIN";

    private CompositeDisposable compositeDisposable;
    private LockableViewPager viewPager;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        updateUI();

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
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
            /*OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(loggingInterceptor);*/


            final OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(300, TimeUnit.SECONDS)
                    .build();

            String serverUrl = sp.getString("server_url", "10.42.0.1");
            //create async client to our server (currently only local network)
            Retrofit mClient = new Retrofit.Builder()
                    .baseUrl("http://" + serverUrl + ":5000/")
//                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();
            sp.edit().putString("server_url", "10.42.0.1").apply();

            Log.i(TAG, "Connect to server...");
            //attach the api requests to it
            ApiService mApiConnection = mClient.create(ApiService.class);

            viewPager = (LockableViewPager) findViewById(R.id.view_pager);

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

    public void updateUI() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        });
    }

    private boolean firstResume = true;
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
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

    @Override
    public void onBackPressed() {
        switch (viewPager.getCurrentItem()){
            case 0:
                viewPager.setCurrentItem(1, true);
                break;
            case 1:
                super.onBackPressed();
                break;
            case 2:
                List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
                for(Fragment f : fragmentList){
                    if(f != null && f instanceof GalleryFragment){
                        if(!((GalleryFragment)f).onBackPressed()){
                            viewPager.setCurrentItem(1, true);
                        }
                    }
                }
                break;
        }
    }
}
