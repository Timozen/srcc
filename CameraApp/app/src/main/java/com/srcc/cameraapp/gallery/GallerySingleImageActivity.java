package com.srcc.cameraapp.gallery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ortiz.touchview.TouchImageView;
import com.srcc.cameraapp.R;

/**
 * This activity will display the image in full screen
 */
public class GallerySingleImageActivity extends Activity {

    /**
     * This function will be called once the act. is created
     * @param savedInstanceState only for super
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the layout
        setContentView(R.layout.view_image_fragment);

        //load the data from the prev. activity
        Intent intent = getIntent();
        Uri imageUri = Uri.parse(intent.getStringExtra("Uri"));

        //Load the image into the view
        TouchImageView imageView = findViewById(R.id.bigimage);
        imageView.setMaxZoom(8);
        //just glide for fast loading and caching
        Glide.with(this).load(imageUri).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
    }
}
