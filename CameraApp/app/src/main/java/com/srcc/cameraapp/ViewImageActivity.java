package com.srcc.cameraapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ortiz.touchview.TouchImageView;


public class ViewImageActivity extends Activity {
    private Uri imageUri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image_fragment);

        Intent intent = getIntent();
        imageUri = Uri.parse(intent.getStringExtra("Uri"));

        TouchImageView imageView = (TouchImageView) findViewById(R.id.bigimage);
        imageView.setMaxZoom(8);
        Glide.with(this).load(imageUri).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
    }
}
