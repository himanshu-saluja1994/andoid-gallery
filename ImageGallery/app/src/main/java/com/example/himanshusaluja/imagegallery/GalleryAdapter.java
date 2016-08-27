package com.example.himanshusaluja.imagegallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.cache.plus.SimpleImageLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by himanshu.saluja on 27/08/16.
 */
public class GalleryAdapter extends BaseAdapter {
    SimpleImageLoader imageLoader;
    private ArrayList<Images> arrayList;
    public Context context;
    public LayoutInflater layoutInflater;
    private int size;
    public GalleryAdapter(Context context, ArrayList<Images> images, SimpleImageLoader imageLoader) {
        this.arrayList = images;
        this.imageLoader = imageLoader;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(this.context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            viewHolder.view = convertView.findViewById(R.id.view_alpha);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        viewHolder.view.getLayoutParams().width = size;
        viewHolder.view.getLayoutParams().height = size;

        if (arrayList.get(position).isSelected) {
            viewHolder.view.setAlpha(0.8f);
            ((FrameLayout) convertView).setForeground(context.getResources().getDrawable(R.drawable.ic_done_white));

        } else {
            viewHolder.view.setAlpha(0.0f);
            ((FrameLayout) convertView).setForeground(null);
        }


        SimpleImageLoader mImageFetcher = imageLoader;
        mImageFetcher.get(Uri.decode(Uri.fromFile(new File(arrayList.get(position).path)).toString()), viewHolder.imageView);

        return convertView;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Images getItem(int position) {
        return arrayList.get(position);
    }
    public void setLayoutParams(int size) {
        this.size = size;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public View view;
    }


}
