package com.srcc.cameraapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class ShowImagesFragment extends Fragment {
    private static final String TAG = "SRCC_SHOW_IMAGES";
    private MyAdapter myAdapter;

    public static ShowImagesFragment newInstance(){
        return new ShowImagesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.show_images_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext().getApplicationContext(), calculateNoOfColumns(getContext().getApplicationContext(), 220));
        recyclerView.setLayoutManager(layoutManager);

        myAdapter = new MyAdapter(getActivity());
        recyclerView.setAdapter(myAdapter);
        myAdapter.changeCursor(queryThumbnails());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        myAdapter.changeCursor(queryThumbnails());
    }

    @Override
    public void onResume() {
        super.onResume();
        myAdapter.changeCursor(queryThumbnails());
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    private Cursor queryThumbnails(){
        ContentResolver cr = getActivity().getContentResolver();
        String[] mProjection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA
        };

        return cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mProjection,
                MediaStore.Images.Media.DATA + " like ? ",
                new String[] {"%srcc%"},
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
    }
}
