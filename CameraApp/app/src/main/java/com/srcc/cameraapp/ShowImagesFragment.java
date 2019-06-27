package com.srcc.cameraapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ShowImagesFragment extends Fragment {
    public static final String TAG = "SRCC_SHOW_IMAGES";

    public static ShowImagesFragment newInstance(){
        return new ShowImagesFragment();
    }

    private final String titles[] = {
            "Image1",
            "Image1",
            "Image1",
            "Image1",
            "Image1",
            "Image1",
            "Image1"
    };
    private final Uri images[] = {
            Uri.parse("android.resource://com.srcc.cameraapp//drawable//ic_camera"),
            Uri.parse("android.resource://com.srcc.cameraapp//drawable//ic_camera"),
            Uri.parse("android.resource://com.srcc.cameraapp//drawable//ic_camera"),
            Uri.parse("android.resource://com.srcc.cameraapp//drawable//ic_camera"),
            Uri.parse("android.resource://com.srcc.cameraapp//drawable//ic_camera"),
            Uri.parse("android.resource://com.srcc.cameraapp//drawable//ic_camera"),
            Uri.parse("android.resource://com.srcc.cameraapp//drawable//ic_camera")
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.show_images_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext().getApplicationContext(), calculateNoOfColumns(getContext().getApplicationContext(), 220));
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<ImageCell> cells = prepareData();
        MyAdapter adapter = new MyAdapter(getContext().getApplicationContext(), cells);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<ImageCell> prepareData() {
        ArrayList<ImageCell> cells = new ArrayList<>();

        for (int i = 0; i < titles.length; i++) {
            ImageCell cell = new ImageCell(titles[i], images[i]);
            cells.add(cell);
        }

        return cells;
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

}
