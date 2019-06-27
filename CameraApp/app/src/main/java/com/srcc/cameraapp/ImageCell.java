package com.srcc.cameraapp;

import android.media.Image;
import android.net.Uri;

public class ImageCell {

    private String title;
    private Uri image;

    public ImageCell(String title, Uri image) {
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }
}
