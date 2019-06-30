package com.srcc.cameraapp.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Entry point of the app
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SRCC_CAMERA_MAIN";

    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //trashcan if somehow the connection is interrupted
        compositeDisposable = new CompositeDisposable();

        //create async client to our server (currently only local network)
        Retrofit mClient = new Retrofit.Builder()
                .baseUrl("http://192.168.178.44:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        Log.i(TAG, "Connect to server...");
        //attach the api requests to it
        ApiService mApiConnection = mClient.create(ApiService.class);

//        re-enable this code once have to check the server connection
//        Single<Home> home = mApiConnection.getHome();
//
//        home.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Home>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                Log.i(TAG, "OnSubscribe triggered");
//                compositeDisposable.add(d);
//            }
//
//            @Override
//            public void onSuccess(Home home) {
//                Log.i(TAG, home.getName());
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.i(TAG, "onError triggered");
//                e.printStackTrace();
//            }
//        });

        ViewPager viewPager = findViewById(R.id.view_pager);

        //create the fragment view for nice swiping
        ViewPagerAdapter vpa = new ViewPagerAdapter.Builder()
                .setFragmentManager(getSupportFragmentManager())
                .setViewPager(viewPager)
                .setClient(mClient)
                .setApiService(mApiConnection)
                .setCompositeDisposable(compositeDisposable)
                .createViewPagerAdapter();

        viewPager.setAdapter(vpa);
        //set the starting page to the camera, currently on frame == 0
        viewPager.setCurrentItem(0);
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

    @Override
    protected void onDestroy() {
        //remove our not yet responded request
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
}
