package com.srcc.cameraapp;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class Utils {

    public static final String IMAGE_FOLDER_NAME = "srcc";

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.i("CAMERA_APP", "Directory not created");
        } else {
            Log.i("CAMERA_APP", "Directory was created");
        }
        return file;
    }
}
