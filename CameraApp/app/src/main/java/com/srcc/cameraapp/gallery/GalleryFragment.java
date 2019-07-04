package com.srcc.cameraapp.gallery;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;

import java.util.Objects;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;

public class GalleryFragment extends Fragment {

    //api variables
    private final CompositeDisposable compositeDisposable;
    private final ApiService mApiConnection;

    /**
     * Builder pattern (not necessary but for consistency)
     */
    public static class Builder {
        private CompositeDisposable compositeDisposable;
        private ApiService mApiConnection;

        public Builder setCompositeDisposable(CompositeDisposable compositeDisposable) {
            this.compositeDisposable = compositeDisposable;
            return this;
        }

        public Builder setmApiConnection(ApiService mApiConnection) {
            this.mApiConnection = mApiConnection;
            return this;
        }

        public GalleryFragment createGalleryFragment() {
            return new GalleryFragment(compositeDisposable, mApiConnection);
        }
    }


    private static final String TAG = "SRCC_SHOW_IMAGES";
    private GalleryAdapter myAdapter;

    private GalleryFragment(CompositeDisposable compositeDisposable, ApiService mApiConnection) {
        this.compositeDisposable = compositeDisposable;
        this.mApiConnection = mApiConnection;
    }

    /**
     * This function is called when the fragment is created. Here we just inflate
     * the corresponding layout
     *
     * @param inflater           basic inflater
     * @param container          fragment container
     * @param savedInstanceState not important for us
     * @return the new created fragment layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate the layout, but don't attach it to the root
        return inflater.inflate(R.layout.gallery_fragment, container, false);
    }


    /**
     * This function is called directly after the onCreateView. Here we add the actual logic
     * to the fragments layout
     *
     * @param view               the newly created view
     * @param savedInstanceState only for the super class
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //find and set our recycler view
        RecyclerView recyclerView = view.findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);

        //load the grid layout manager with 2 columns TODO maybe more?
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(Objects.requireNonNull(getContext()).getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        //Load our gallery adapter to display the data after our rules
        myAdapter = new GalleryAdapter(getActivity(), compositeDisposable, mApiConnection);
        recyclerView.setAdapter(myAdapter);

        //load the data into the cursor
        myAdapter.changeCursor(queryThumbnails());
    }

    /**
     * This function will be called once we go back to the app.
     * Just update the recycler view
     *
     * @param savedInstanceState only for super
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        myAdapter.changeCursor(queryThumbnails());
    }

    /**
     * This function will be called once we go back to the app after pause
     * Just update the recycler view
     */
    @Override
    public void onResume() {
        super.onResume();
        myAdapter.changeCursor(queryThumbnails());
    }

    /**
     * This function will query the MediaStore to get the thumbnails of our
     * pictures. The cursor will hold all the needed information inside.
     *
     * @return the cursor with the data
     */
    private Cursor queryThumbnails() {
        Log.i(TAG, "Query thumbnails");
        //get the content resolver which will take care of all the file handling
        ContentResolver cr = Objects.requireNonNull(getActivity(), "Activity should not be null").getContentResolver();

        //which data we want to get from the media store
        //id for row location, data_added for sorting, data for the actual location
        String[] mProjection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA
        };

        //query the media store to get the data
        //query the external storage only (also the place were we save everything)
        //only query data which has "srcc" in the path and sort DESC
        return cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mProjection,
                MediaStore.Images.Media.DATA + " like ? ",
                new String[]{"%srcc%"},
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
    }
}
