package com.srcc.cameraapp.other;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.settings.SettingsFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;

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
import okio.BufferedSink;

/**
 * This class collects all the useful functions we might need to use.
 */
public class Utils extends Application {
        public static final String IMAGE_FOLDER_NAME = "srcc";

    /**
     * Check if we can write in the external storage
     *
     * @return is writable
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * This will return the correct File location of the external
     * directory. This is we can get everywhere the same paths.
     *
     * @param albumName Name of the public album folder we want to find
     * @return the File object of album name
     */
    public static File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.i("CAMERA_APP", "Directory not created");
        } else {
            Log.i("CAMERA_APP", "Directory was created");
        }
        return file;
    }

    public static void sendImage(ApiService api, File image, CompositeDisposable compositeDisposable, String timeValue, Context context){
        String TAG = "SEND_IMAGE";
        RequestBody requestFile = RequestBody.create(MediaType.parse("image"), image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", image.getName(), requestFile);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int backend = preferences.getInt("backend", 0);

        String name;
        switch (backend) {
            case 0:
                name = "srdense";
                break;
            case 1:
                name = "srresnet";
                break;
            case 2:
                name = "srgan";
                break;
            default:
                name = "";
                break;
        }
        int tiling = preferences.getBoolean(name + "_use_tiling", true) ? 1 : 0;
        int tile_size = (preferences.getInt(name + "_tiling_size", 0) + 1) * SettingsFragment.SRDENSE_TILE_SIZE;
        int stitch_style = preferences.getInt(name + "_stitch_style", 0);
        int overlap = preferences.getBoolean(name + "_use_overlap", true) ? 1 : 0;
        int adjust_brightness = preferences.getBoolean(name + " _adjust_brightness", true) ? 1 : 0;
        int use_hsv = preferences.getBoolean(name + "_use_hsv", true) ? 1 : 0;
        int initialization = preferences.getBoolean("srgan_use_init", true) ? 1 : 0;

        Single<ResponseBody> single;
        boolean debug = preferences.getBoolean("debug", true);
        if(debug){
             single =  api.sendImage(debug ? 1 : 0, backend, tiling, tile_size, stitch_style, overlap, adjust_brightness, use_hsv, initialization, body);
        } else {
             single =  api.sendImage(backend, tiling, tile_size, stitch_style, overlap, adjust_brightness, use_hsv, initialization, body);
        }
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
                    File root = Utils.getPublicAlbumStorageDir(Utils.IMAGE_FOLDER_NAME);
                    File hrImage = new File(root.getAbsolutePath(), timeValue + "_hr.jpg");

                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        byte[] fileReader = new byte[4096];

                        long fileSize = responseBody.contentLength();
                        long fileSizeDownloaded = 0;

                        inputStream = responseBody.byteStream();
                        outputStream = new FileOutputStream(hrImage);

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
                        Toast.makeText(context, "Received SR Image from server!", Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        if (outputStream != null) {
                            outputStream.close();
                        }
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(hrImage)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Upload somehow failed");
                Log.e(TAG, e.getMessage());
                e.printStackTrace();

                Toast.makeText(context, "Server couldn't be reached! Try later again.", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
