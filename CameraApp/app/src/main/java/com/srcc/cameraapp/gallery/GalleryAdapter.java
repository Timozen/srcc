package com.srcc.cameraapp.gallery;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.srcc.cameraapp.R;


public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Activity activity;
    private Cursor galleryCursor;

    GalleryAdapter(final Activity activity) {
        this.activity = activity;
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
        view.setOnClickListener(vh);
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
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewTitle;
        private ImageView imageViewThumbnail;
        private Uri mUri;

        ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.textView_image_title);
            imageViewThumbnail = view.findViewById(R.id.imageView_gallery_item_image);
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
            textViewTitle.setText(temp2[temp2.length - 1]);
        }

        /**
         * If we click on one element we want to open a new activity were can take
         * a closer look
         *
         * @param v the view of the fragment
         */
        @Override
        public void onClick(View v) {

            //Start a new GallerySingleImageActivity and give the Uri of the clicked imageView_gallery_item_image
            Intent intent = new Intent(activity, GallerySingleImageActivity.class);
            intent.putExtra("Uri", mUri.toString());
            activity.startActivity(intent);
        }
    }
}
