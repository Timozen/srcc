package com.srcc.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.api.Home;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SRCC_CAMERA_MAIN";

    private CompositeDisposable compositeDisposable;
    private Retrofit mClient;
    private ApiService mApiConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //trashcan if somehow the connection is interrupted
        compositeDisposable = new CompositeDisposable();

        //create async client to our server
        mClient = new Retrofit.Builder()
                .baseUrl("http://192.168.178.44:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        //attach the api requests to it
        mApiConnection = mClient.create(ApiService.class);

        Single<Home> home = mApiConnection.getHome();

        home.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Home>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i(TAG, "OnSubscribe triggered");
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(Home home) {
                Log.i(TAG, home.getName());
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onError triggered");
                e.printStackTrace();
            }
        });

        //uploadFile(Uri.parse("android.resource://com.srcc.camerapp/mipmap/ic_launcher/ic_launcher"));

        ViewPager viewPager = findViewById(R.id.view_pager);

        //create the fragment view for nice swiping
        ViewPagerAdapter vpa = new ViewPagerAdapter.ViewPagerAdapterBuilder()
                .setmFragmentManager(getSupportFragmentManager())
                .setmViewpager(viewPager)
                .setClient(mClient)
                .setApiService(mApiConnection)
                .setCompositeDisposable(compositeDisposable)
                .createViewPagerAdapter();

        viewPager.setAdapter(vpa);
        viewPager.setCurrentItem(1);
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onDestroy() {
        if(!compositeDisposable.isDisposed()){
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
}
