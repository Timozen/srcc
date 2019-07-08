package com.srcc.cameraapp.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.material.snackbar.Snackbar;
import com.ortiz.touchview.TouchImageView;
import com.srcc.cameraapp.R;
import com.srcc.cameraapp.api.ApiService;
import com.srcc.cameraapp.other.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.disposables.CompositeDisposable;


public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Activity activity;
    private Cursor galleryCursor;
    private final CompositeDisposable compositeDisposable;
    private ApiService mApiConnection;
    private int rowCount;
    private int width;

    private int shortAnimationDuration;
    private int currentPosition;
    private Animator currentAnimator;

    private HashMap<Integer, ViewHolder> viewHolderList;

    private ConstraintLayout display;
    private ConstraintLayout gallery;

    private TouchImageView touchImageView;
    private float startScaleFinal;
    private boolean itemWasDeleted = false;
    private ViewPager viewPagerFullScreen;
    public boolean isFullScreen;

    GalleryAdapter(final Activity activity, final CompositeDisposable compositeDisposable, final ApiService apiService, int rowCount) {
        this.activity = activity;
        this.mApiConnection = apiService;
        this.compositeDisposable = compositeDisposable;
        this.rowCount = rowCount;

        DisplayMetrics dp = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dp);
        width = dp.widthPixels / rowCount;
        shortAnimationDuration = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

        viewHolderList = new HashMap<>();

        gallery = activity.findViewById(R.id.constraintLayout_gallery_main);
        display = activity.findViewById(R.id.display);

        activity.findViewById(R.id.button_gallery_image_edit).setOnClickListener(v -> {
            ViewHolder vh = viewHolderList.get(currentPosition);

            if(!Utils.isSendingImage() && vh.isLR) {
                File f = new File(vh.getUri().getPath());
                Utils.sendImage(mApiConnection, f, compositeDisposable, vh.getTitle().split("_")[0], activity.getApplicationContext());
            } else if(!vh.isLR) {
                Snackbar.make(display, activity.getApplicationContext().getString(R.string.settings_text_notify_is_hr_image), Snackbar.LENGTH_SHORT).show();
            } else{
                Snackbar.make(display, activity.getApplicationContext().getString(R.string.settings_text_notify_one_image), Snackbar.LENGTH_SHORT).show();
            }
        });

        activity.findViewById(R.id.button_gallery_item_delete).setOnClickListener(v -> {
            itemWasDeleted = true;
            ViewHolder current = viewHolderList.get(currentPosition);
            File toDelete = new File(current.getUri().getPath());

            //check right one
            ViewHolder next = viewHolderList.get(currentPosition + 1);
            if(next != null){
                currentPosition += 1;
                viewPagerFullScreen.setCurrentItem(currentPosition, true);
                toDelete.delete();
                activity.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(toDelete)));
                return;
            }
            next = viewHolderList.get(currentPosition - 1);
            if(next != null){
                currentPosition -= 1;
                viewPagerFullScreen.setCurrentItem(currentPosition, true);
                toDelete.delete();
                activity.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(toDelete)));
                return;
            }
            currentPosition = -1;
            toDelete.delete();
            activity.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(toDelete)));
            returnFromFullImage();
        });
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
        //noinspection SuspiciousNameCombination
        vh.getImageView().getLayoutParams().height = width;
        vh.getImageView().getLayoutParams().width = width;
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

        viewHolderList.put(position, viewHolder);

        viewHolder.getImageView().setOnClickListener(v -> {
            currentPosition = position;
            viewPagerFullScreen = activity.findViewById(R.id.viewPager_fullscreen);
            viewPagerFullScreen.setPageTransformer(true, new DepthPageTransformer());

            FullScreenPager pg = new FullScreenPager(activity.getApplicationContext());
            viewPagerFullScreen.setAdapter(pg);
            viewPagerFullScreen.addOnPageChangeListener(pg);
            viewPagerFullScreen.setCurrentItem(position);
            goToFullImage();
        });

        viewHolder.getView().findViewById(R.id.textView_gallery_sr_indicator).setVisibility(viewHolder.isLR ? TextView.INVISIBLE : TextView.VISIBLE);
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
     * This function will query the MediaStore to get the thumbnails of our
     * pictures. The cursor will hold all the needed information inside.
     *
     * @return the cursor with the data
     */
    Cursor queryThumbnails() {
        //get the content resolver which will take care of all the file handling
        ContentResolver cr = Objects.requireNonNull(activity, "Activity should not be null").getContentResolver();

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
                new String[]{"%/srcc/%"},
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
    }

    private void goToFullImage(){
        if(currentAnimator != null){
            currentAnimator.cancel();
        }

        ViewHolder currentViewHolder = viewHolderList.get(currentPosition);
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        currentViewHolder.getImageView().getGlobalVisibleRect(startBounds);
        gallery.getGlobalVisibleRect(finalBounds, globalOffset);

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

        startScaleFinal = startScale;
        isFullScreen = true;
    }

    public void returnFromFullImage(){

        currentPosition = viewPagerFullScreen.getCurrentItem();

        if(itemWasDeleted){
            itemWasDeleted = false;
            display.setVisibility(View.GONE);
            changeCursor(queryThumbnails());
            return;
        }
        changeCursor(queryThumbnails());
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }


        ViewHolder currentViewHolder = viewHolderList.get(currentPosition);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        currentViewHolder.getImageView().getGlobalVisibleRect(startBounds);
        gallery.getGlobalVisibleRect(finalBounds, globalOffset);

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

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set1 = new AnimatorSet();
        set1.play(ObjectAnimator.ofFloat(display, View.X, startBounds.left))
                .with(ObjectAnimator.ofFloat(display,View.Y, startBounds.top))
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
        isFullScreen=false;
    }

    public boolean onBackPressed(){
        if (isFullScreen){
            returnFromFullImage();
            return true;
        }
        return false;
    }


    /**
     * This class will hold the information of our elements in the recycler view
     */
    class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageViewThumbnail;
        private Uri mUri;
        private String title;
        private boolean isLR;
        private boolean isSelected;
        private View view;

        ViewHolder(View view) {
            super(view);
            imageViewThumbnail =  view.findViewById(R.id.imageView_gallery_item_image);
            isSelected = false;
            this.view = view;
        }

        ImageView getImageView() {
            return imageViewThumbnail;
        }

        void setUri(Uri mUri) {
            this.mUri = mUri;
        }

        void setTitle(String title) {
            String temp = title.split("\\.")[0];
            String[] temp2 = temp.split("/");
            this.title = temp2[temp2.length - 1];
            temp2 = this.title.split("_");
            this.isLR = temp2[temp2.length - 1].equals("lr");
        }

        boolean getIsLR(){
            return isLR;
        }

        Uri getUri() {
            return mUri;
        }

        String getTitle() {
            return title;
        }

        public View getView() {
            return view;
        }
    }

    class FullScreenPager extends PagerAdapter implements ViewPager.OnPageChangeListener {
        private Context context;

        FullScreenPager(Context context){
            this.context = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(context).inflate(R.layout.gallery_item_view_full, null);
            TouchImageView touchImageView = view.findViewById(R.id.expanded_image);

            ViewHolder vh = viewHolderList.get(position);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            if(!sp.getBoolean("interpolation", false)){
                try {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);


                    Bitmap bitmap = null;
                    BitmapDrawable drawable = null;
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), vh.getUri());

                    if(vh.isLR) {
                        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        drawable = new BitmapDrawable(context.getResources(), rotatedBitmap);
                    } else {
                        drawable = new BitmapDrawable(context.getResources(), bitmap);
                    }

                    drawable.setFilterBitmap(false);

                    touchImageView.setImageDrawable(drawable);

                } catch (IOException e) {
                    touchImageView.setImageResource(R.drawable.ic_mood_black_128dp);
                    Glide.with(activity).load(viewHolderList.get(position).getUri())
                            .thumbnail(0.5f)
                            .placeholder(R.drawable.ic_mood_black_128dp)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(touchImageView);
                }
            } else {
                touchImageView.setImageResource(R.drawable.ic_mood_black_128dp);
                Glide.with(activity).load(viewHolderList.get(position).getUri())
                        .thumbnail(0.5f)
                        .placeholder(R.drawable.ic_mood_black_128dp)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(touchImageView);
            }






            touchImageView.setZoom(1f);

            touchImageView.setOnClickListener(v -> returnFromFullImage());
            touchImageView.setMaxZoom(8);
            container.addView(touchImageView);
            return touchImageView;
        }



        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return viewHolderList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }

}
