package com.srcc.cameraapp.other;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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


}
