package com.srcc.cameraapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        myAdapter = new MyAdapter(getActivity(), getFragmentManager());
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
