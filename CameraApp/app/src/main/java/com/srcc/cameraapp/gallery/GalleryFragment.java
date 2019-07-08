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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.main.LockableViewPager;

import java.util.Objects;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;

public class GalleryFragment extends Fragment {

    //api variables
    private final CompositeDisposable compositeDisposable;
    private ApiService mApiConnection;

    /**
     * Builder pattern (not necessary but for consistency)
     */
    public static class Builder {
        private CompositeDisposable compositeDisposable;
        private ApiService mApiConnection;
        private LockableViewPager viewPager;

        public Builder setCompositeDisposable(CompositeDisposable compositeDisposable) {
            this.compositeDisposable = compositeDisposable;
            return this;
        }

        public Builder setmApiConnection(ApiService mApiConnection) {
            this.mApiConnection = mApiConnection;
            return this;
        }

        public Builder setViewPager(LockableViewPager viewPager){
            this.viewPager = viewPager;
            return this;
        }

        public GalleryFragment createGalleryFragment() {
            return new GalleryFragment(compositeDisposable, mApiConnection, viewPager);
        }

    }


    private static final String TAG = "SRCC_SHOW_IMAGES";
    private GalleryAdapter myAdapter;
    private LockableViewPager viewPager;
    private GalleryFragment(CompositeDisposable compositeDisposable, ApiService mApiConnection, LockableViewPager viewPager) {
        this.compositeDisposable = compositeDisposable;
        this.mApiConnection = mApiConnection;
        this.viewPager = viewPager;
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
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(Objects.requireNonNull(getContext()).getApplicationContext(), 3){
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);

        //Load our gallery adapter to display the data after our rules
        myAdapter = new GalleryAdapter(getActivity(), compositeDisposable, mApiConnection, 3, viewPager);
        recyclerView.setAdapter(myAdapter);

        //load the data into the cursor
        myAdapter.changeCursor(myAdapter.queryThumbnails());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            myAdapter.changeCursor(myAdapter.queryThumbnails());
        }
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
        myAdapter.changeCursor(myAdapter.queryThumbnails());
    }

    /**
     * This function will be called once we go back to the app after pause
     * Just update the recycler view
     */
    @Override
    public void onResume() {
        super.onResume();
        myAdapter.changeCursor(myAdapter.queryThumbnails());
    }

    public boolean onBackPressed(){
        return myAdapter.onBackPressed();
    }

}
