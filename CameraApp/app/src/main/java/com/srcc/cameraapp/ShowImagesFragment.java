package com.srcc.cameraapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class ShowImagesFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {
    public static final String TAG = "SRCC_SHOW_IMAGES";

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

        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);

        view.setOnTouchListener(this);
    }



    @Override
    public void onClick(View v) {
        returnToPrev();
    }

    private void returnToPrev(){
        getFragmentManager().popBackStack();
    }

    final GestureDetector gesture = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i(TAG, "onFling has been called!");
            final int SWIPE_MIN_DISTANCE = 120;
            final int SWIPE_MAX_OFF_PATH = 250;
            final int SWIPE_THRESHOLD_VELOCITY = 200;
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.i(TAG, "Right to Left");

                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                   returnToPrev();
                }
            } catch (Exception e) {
                // nothing
            }
            return true; //super.onFling(e1, e2, velocityX, velocityY);
        }
    });

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gesture.onTouchEvent(event);
        return true;
    }
}
