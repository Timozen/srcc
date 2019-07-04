package com.srcc.cameraapp.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.other.Utils;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;


public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Activity activity;
    private Cursor galleryCursor;
    private final CompositeDisposable compositeDisposable;
    private final ApiService mApiConnection;
    private int rowCount;
    private int width;

    GalleryAdapter(final Activity activity, final CompositeDisposable compositeDisposable, final ApiService apiService, int rowCount) {
        this.activity = activity;
        this.mApiConnection = apiService;
        this.compositeDisposable = compositeDisposable;
        this.rowCount = rowCount;

        DisplayMetrics dp = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dp);
        width = dp.widthPixels / rowCount;

        Log.i("Gallery", "width per item " + width);
    }

    /**
     * This function will be called if android creates a element in the recycler view.
     * Now we have to attach our element layout ot it so we can bind our data later.
     *
     * @param viewGroup this is the "root" for the elements
     * @param position  new elements id in the recycler view
     * @return the newly created ViewHolder, which hold our element
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        //inflate the element layout to the root
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gallery_item_view, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        //set the onclick listener so we can handle clicks
//        view.setOnClickListener(vh);
//        view.setOnLongClickListener(vh);
        vh.getImageView().getLayoutParams().width = width;
        vh.getImageView().getLayoutParams().height = width;
        return vh;
    }

    /**
     * In this function we can connect the data and the elements behind it
     *
     * @param viewHolder this is the "root" for the elements
     * @param position   position the in the recycler view
     */
    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ViewHolder viewHolder, int position) {
        //load the imageView_gallery_item_image Uri from our MediaStore (should be descending order)
        Uri imageUri = getUriFromMediaStore(position);
        //Use Glide to load the imageView_gallery_item_image and cache them in to the according imageView_gallery_item_image view
        Glide.with(activity).load(imageUri).centerCrop().into(viewHolder.getImageView());
        //attach the data
        viewHolder.setUri(imageUri);
        viewHolder.setTitle(imageUri.toString());
    }

    /**
     * @return How many elements we are displaying == same as in media store
     */
    @Override
    public int getItemCount() {
        return (galleryCursor == null) ? 0 : galleryCursor.getCount();
    }

    /**
     * This function will update our MediaStore Cursor so we can update
     * the recycler view content
     *
     * @param newCursor the new cursor to check
     * @return the old cursor before the swap
     */
    private Cursor swapCursor(Cursor newCursor) {
        //if save we don't have to anything
        if (galleryCursor == newCursor) {
            return null;
        }

        Cursor oldCursor = galleryCursor;
        galleryCursor = newCursor;
        //check if there is anything new
        if (galleryCursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    /**
     * Insert the new cursor of the MediaStore
     * If there is still data left close it
     *
     * @param newCursor MediaStore update cursor
     */
    void changeCursor(Cursor newCursor) {
        Cursor oldCursor = swapCursor(newCursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    /**
     * This function will give the Uri of the file on the
     * external storage.
     *
     * @param position of element in the recycler view
     * @return the Uri of the corresponding file
     */
    private Uri getUriFromMediaStore(int position) {
        //get the index of the data column
        int dataIndex = galleryCursor.getColumnIndex(MediaStore.Images.Media.DATA);

        //we want to load the data from the position == row (in this case)
        galleryCursor.moveToPosition(position);
        //get the combination of row and column (which is the path of the file)
        String dataString = galleryCursor.getString(dataIndex);
        //make the path to usable Uri
        return Uri.parse("file://" + dataString);
    }

    /**
     * This class will hold the information of our elements in the recycler view
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageViewThumbnail;
        private Uri mUri;
        private String title;
        private boolean isSelected;
        private Animator currentAnimator;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(View view) {
            super(view);
            imageViewThumbnail =  view.findViewById(R.id.imageView_gallery_item_image);
            isSelected = false;

            int shortAnimationDuration = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);
            currentAnimator = null;

            imageViewThumbnail.setOnClickListener(v -> {
                if(currentAnimator != null){
                    currentAnimator.cancel();
                }
                ConstraintLayout display = activity.findViewById(R.id.display);
                ImageView imageViewLarge = activity.findViewById(R.id.expanded_image);


                Log.i("gallery", "image view is " + imageViewLarge.toString());

//                Glide.with(activity).load(mUri).centerCrop().into(imageViewLarge);
                imageViewLarge.setImageURI(mUri);

                final Rect startBounds = new Rect();
                final Rect finalBounds = new Rect();
                final Point globalOffset = new Point();

                imageViewThumbnail.getGlobalVisibleRect(startBounds);
                activity.findViewById(R.id.constraintLayout_gallery_main).getGlobalVisibleRect(finalBounds, globalOffset);

                startBounds.offset(-globalOffset.x, -globalOffset.y);
                finalBounds.offset(-globalOffset.x, -globalOffset.y);

                float startScale;
                if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
                    // Extend start bounds horizontally
                    startScale = (float) startBounds.height() / finalBounds.height();
                    float startWidth = startScale * finalBounds.width();
                    float deltaWidth = (startWidth - startBounds.width()) / 2;
                    startBounds.left -= deltaWidth;
                    startBounds.right += deltaWidth;
                } else {
                    // Extend start bounds vertically
                    startScale = (float) startBounds.width() / finalBounds.width();
                    float startHeight = startScale * finalBounds.height();
                    float deltaHeight = (startHeight - startBounds.height()) / 2;
                    startBounds.top -= deltaHeight;
                    startBounds.bottom += deltaHeight;
                }

                display.setVisibility(View.VISIBLE);
                display.setPivotX(0f);
                display.setPivotY(0f);

                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(display, View.X, startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(display, View.Y, startBounds.top, finalBounds.top))
                    .with(ObjectAnimator.ofFloat(display, View.SCALE_X, startScale, 1f))
                    .with(ObjectAnimator.ofFloat(display, View.SCALE_Y, startScale, 1f));

                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;

                final float startScaleFinal = startScale;
                imageViewLarge.setOnClickListener(view1 -> {
                    if (currentAnimator != null) {
                        currentAnimator.cancel();
                    }

                    // Animate the four positioning/sizing properties in parallel,
                    // back to their original values.
                    AnimatorSet set1 = new AnimatorSet();
                    set1.play(ObjectAnimator.ofFloat(display, View.X, startBounds.left))
                            .with(ObjectAnimator.ofFloat(display,View.Y,startBounds.top))
                            .with(ObjectAnimator.ofFloat(display,View.SCALE_X, startScaleFinal))
                            .with(ObjectAnimator.ofFloat(display,View.SCALE_Y, startScaleFinal));

                    set1.setDuration(shortAnimationDuration);
                    set1.setInterpolator(new DecelerateInterpolator());
                    set1.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            display.setVisibility(View.GONE);
                            currentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            display.setVisibility(View.GONE);
                            currentAnimator = null;
                        }
                    });
                    set1.start();
                    currentAnimator = set1;
                });

            });

            imageViewThumbnail.setOnLongClickListener(v -> {
                if(isSelected) {
                    imageViewThumbnail.setColorFilter(Color.argb(0, 0, 0, 0));
                } else {
                    imageViewThumbnail.setColorFilter(Color.argb(50, 0, 0, 255));
                }
                isSelected = !isSelected;
                return isSelected;
            });
        }

        ImageView getImageView() {
            return imageViewThumbnail;
        }

        void setUri(Uri mUri) {
            this.mUri = mUri;
        }

        /**
         * Parse the title which should be always some path/numbers_{lr/hr}.jpg
         *
         * @param title mostly the filepath
         */
        void setTitle(String title) {
            String temp = title.split("\\.")[0];
            String[] temp2 = temp.split("\\/");
            this.title = temp2[temp2.length - 1];
//            textViewTitle.setText(this.title);
        }

        /**
         * If we click on one element we want to open a new activity were can take
         * a closer look
         *
         * @param v the view of the fragment
         *//*
        @Override
        public void onClick(View v) {

            //Start a new GallerySingleImageActivity and give the Uri of the clicked imageView_gallery_item_image
            Intent intent = new Intent(activity, GallerySingleImageActivity.class);
            intent.putExtra("Uri", mUri.toString());
            activity.startActivity(intent);
        }


        @Override
        public boolean onLongClick(View v) {

            imageViewThumbnail.setColorFilter(Color.argb(50, 0, 0, 0));

            Snackbar snackbar = Snackbar.make(v, "Create SR Version of image?", Snackbar.LENGTH_LONG);
            snackbar.setAction("CREATE", v1 -> {
                File f = new File(mUri.getPath());
                Utils.sendImage(mApiConnection, f, compositeDisposable, this.title.split("_")[0], activity.getApplicationContext());
            });
            snackbar.show();

            return true;
        }*/
    }
}
