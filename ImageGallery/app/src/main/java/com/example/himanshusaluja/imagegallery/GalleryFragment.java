package com.example.himanshusaluja.imagegallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.cache.DiskLruBasedCache;
import com.android.volley.cache.plus.SimpleImageLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by himanshu.saluja on 27/08/16.
 */
public class GalleryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "gallery fragment";
    public static final String ALBUM = "album";
    public static final String ALL_PHOTOS = "All Photos";
    public static final String SELECTED_PHOTOS = "selected photos";
    public final String SELECTED_PHOTOS_INDEX = "selected photos index";


    private SimpleImageLoader mImageFetcher;
    private GalleryAdapter adapter;
    private View progressBar;
    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
    private ArrayList<Images> images;
    View mRootview;
    GridView gridView;
    private int selectedImageCount = 0;
    private OnSelectionListner mCallback;
    private TextView errorView;
    public ArrayList<Integer> selectedIndex;



    public interface OnSelectionListner {
        public void onSelect(int count);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnSelectionListner) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSelectionListner");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        String album = "";
        if (args != null) {
            album = args.getString(ALBUM, "");
        }
        if (TextUtils.isEmpty(album) || album.equalsIgnoreCase(ALL_PHOTOS)) {
            cursorLoader = new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_MODIFIED+" DESC");
        } else {
            cursorLoader = new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{album}, MediaStore.Images.Media.DATE_MODIFIED+" DESC");
        }
        return cursorLoader;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Images> temp = new ArrayList<>(data.getCount());

        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            do {
                long id = data.getLong(data.getColumnIndex(projection[0]));
                String name = data.getString(data.getColumnIndex(projection[1]));
                String path = data.getString(data.getColumnIndex(projection[2]));
                File file = new File(path);
                if (file.exists()) {
                    temp.add(new Images(id, name, path, false));
                }
            } while (data.moveToNext());

            images = temp;
            selectedImageCount = 0;
            if (selectedIndex != null) {
                for (int i = 0; i < selectedIndex.size(); i++) {
                    images.get(selectedIndex.get(i)).isSelected = true;
                    selectedImageCount++;
                }
            }

            adapter = new GalleryAdapter(getActivity(), images,getmImageFetcher());

            final WindowManager windowManager = (WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            final DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int size = metrics.widthPixels / 3;

            adapter.setLayoutParams(size);
            gridView.setNumColumns(3);
            gridView.setAdapter(adapter);
            gridView.setVisibility(View.VISIBLE);


            mCallback.onSelect(selectedImageCount);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    toggleSelection(position);
                    mCallback.onSelect(selectedImageCount);
                }
            });
        }else{
            gridView.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
        }



    }

    private void toggleSelection(int position) {

        images.get(position).isSelected = !images.get(position).isSelected;
        if (images.get(position).isSelected) {
            selectedImageCount++;
        } else {
            selectedImageCount--;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public ArrayList<String> getSelectedImage() {
        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).isSelected) {
                temp.add(images.get(i).path);
            }
        }
        return temp;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootview =  inflater.inflate(R.layout.fragment_gallery1, container, false);
        gridView = (GridView) mRootview.findViewById(R.id.grid_view_album_select);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                images.get(position).isSelected = !images.get(position).isSelected;
                adapter.notifyDataSetChanged();
            }
        });
        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getIntegerArrayList(SELECTED_PHOTOS_INDEX);
        }
        errorView = (TextView) mRootview.findViewById(R.id.no_images_found);
        errorView.setVisibility(View.GONE);
        return mRootview;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    private SimpleImageLoader getmImageFetcher(){
        if (mImageFetcher == null) {
            DiskLruBasedCache.ImageCacheParams cacheParams = new DiskLruBasedCache.ImageCacheParams(getActivity().getApplicationContext(), "gallery_image");
            cacheParams.setMemCacheSizePercent(0.5f);
            mImageFetcher = new SimpleImageLoader(getActivity().getApplicationContext(), cacheParams);
            ArrayList<Drawable> placeHolderDrawables = new ArrayList<Drawable>();
            placeHolderDrawables.add(getResources().getDrawable(R.drawable.image_placeholder));
            mImageFetcher.setDefaultDrawables(placeHolderDrawables);
            mImageFetcher.setMaxImageSize(512);
            mImageFetcher.setFadeInImage(true);
        }
        return mImageFetcher;
    }

}
