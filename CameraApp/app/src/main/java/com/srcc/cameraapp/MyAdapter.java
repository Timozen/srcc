package com.srcc.cameraapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private Activity mActivity;
    private Context mContext;
    private FragmentManager mFragmentManager;
    private Cursor mGalleryCursor;

    public MyAdapter(final Activity activity, FragmentManager fragmentManager){
        mActivity = activity;
        mContext = mActivity.getApplicationContext();
        mFragmentManager = fragmentManager;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_cell, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder viewHolder, int i) {
        Uri imageUri = getUriFromMediaStore(i);
        Glide.with(mActivity).load(imageUri).centerCrop().into(viewHolder.getImageView());
        viewHolder.setUri(imageUri);
        viewHolder.setTitle(imageUri.toString());
    }

    @Override
    public int getItemCount() {
        return (mGalleryCursor == null) ? 0 : mGalleryCursor.getCount();
    }

    private Cursor swapCursor(Cursor cursor){
        if(mGalleryCursor == cursor){
            return null;
        }
        Cursor oldCursor = mGalleryCursor;
        mGalleryCursor = cursor;
        if(mGalleryCursor != null){
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    public void changeCursor(Cursor cursor){
        Cursor oldCursor = swapCursor(cursor);
        if(oldCursor != null){
            oldCursor.close();
        }
    }

    private Uri getUriFromMediaStore(int position){
        int dataIndex = mGalleryCursor.getColumnIndex(MediaStore.Images.Media.DATA);

        mGalleryCursor.moveToPosition(position);
        String dataString = mGalleryCursor.getString(dataIndex);
        return Uri.parse("file://" + dataString);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTitle;
        private ImageView mImageView;
        private Uri mUri;

        public ViewHolder(View view) {
            super(view);
            mTitle = view.findViewById(R.id.title);
            mImageView = view.findViewById(R.id.image);
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public void setUri(Uri mUri) {
            this.mUri = mUri;
        }

        public void setTitle(String title) {
            String temp = title.split("\\.")[0];
            String temp2[] = temp.split("\\/");
            mTitle.setText(temp2[temp2.length-1]);
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(mActivity, ViewImageActivity.class);
            intent.putExtra("Uri", mUri.toString());
            mActivity.startActivity(intent);

        }
    }
}
