package com.srcc.cameraapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "SRCC_CAMERA_MAIN";
    //mülleimer für fehlerhafte pakete
    CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        compositeDisposable = new CompositeDisposable();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.178.44:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Single<Home> home = apiService.getHome();

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

        uploadFile(Uri.parse("android.resource://com.srcc.camerapp/mipmap/ic_launcher/ic_launcher"));

        /*ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), viewPager));
        viewPager.setCurrentItem(1);*/
    }

    private void uploadFile(Uri uri){
        File root = Utils.getPublicAlbumStorageDir(Utils.IMAGE_FOLDER_NAME);
        String timeValue = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        File file = new File(root.getAbsolutePath(), timeValue + "_lr.jpg");

        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher_test_image);

        if(bm == null){
            Log.e(TAG, "Bitmap is null");
        }


        FileOutputStream output = null;

        try {
            output = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.i(TAG, file.getAbsolutePath());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.178.44:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image"), file);

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        RequestBody description = RequestBody.create(MultipartBody.FORM, "description...");

        Single<ResponseBody> single = apiService.sendImage(description, body);

        single.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i(TAG, "OnSubscribe triggered");
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                Log.i(TAG, "Upload successful and got return");


                try {
                    // todo change the file location/name according to your needs
                    File root = Utils.getPublicAlbumStorageDir(Utils.IMAGE_FOLDER_NAME);
                    String timeValue = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                    File futureStudioIconFile = new File(root.getAbsolutePath(), timeValue + "_hr.jpg");

                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        byte[] fileReader = new byte[4096];

                        long fileSize = responseBody.contentLength();
                        long fileSizeDownloaded = 0;

                        inputStream = responseBody.byteStream();
                        outputStream = new FileOutputStream(futureStudioIconFile);

                        while (true) {
                            int read = inputStream.read(fileReader);

                            if (read == -1) {
                                break;
                            }

                            outputStream.write(fileReader, 0, read);

                            fileSizeDownloaded += read;

                            Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                        }

                        outputStream.flush();

                    } catch (IOException e) {
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } catch (IOException e) {
                }

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Upload somehow failed");
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        });

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
