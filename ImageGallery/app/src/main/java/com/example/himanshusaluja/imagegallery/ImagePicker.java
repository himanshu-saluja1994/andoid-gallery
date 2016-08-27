package com.example.himanshusaluja.imagegallery;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

public class ImagePicker extends AppCompatActivity implements GalleryFragment.OnSelectionListner {

    private Toolbar mToolBar;
    private ListView albumList;
    PopupWindow popupWindow;
    String album = GalleryFragment.ALL_PHOTOS;
    private final String[] requiredPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private final String FROM_BOTTOM_SHEET = "isselected";

    HashSet<String> albumNames;
    AlbumNameAdapter itemsAdapter;
    LinearLayout albumNameLL;
    TextView titleAlbum;
    ImageView imageView;
    MenuItem doneItem;
    boolean flag = false;
    private GalleryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        initActionBar();
        if (savedInstanceState != null){
            album = savedInstanceState.getString(GalleryFragment.ALBUM);
        }
        imageView = (ImageView) findViewById(R.id.icon_drop_down);
        albumNameLL = (LinearLayout) findViewById(R.id.album_name_ll);
        showGalleryFragment(album);
        albumNameLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag)
                    showPopup(ImagePicker.this);
            }
        });
    }

    private void showGalleryFragment(String album) {

        Bundle bundle = new Bundle();
        bundle.putString(GalleryFragment.ALBUM, album);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(GalleryFragment.TAG);
        if (fragment == null) {
            Fragment galleryFragment = new GalleryFragment();
            galleryFragment.setArguments(getIntent().getExtras());
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, galleryFragment, GalleryFragment.TAG);
            ft.commitAllowingStateLoss();
        } else {
            if (fragment instanceof GalleryFragment) {
                GalleryFragment galleryFragment = ((GalleryFragment) fragment);
                if (galleryFragment.selectedIndex != null) {
                    galleryFragment.selectedIndex.clear();
                }
                galleryFragment.getLoaderManager().restartLoader(0, bundle, ((LoaderManager.LoaderCallbacks) fragment));
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState ) {
        super.onSaveInstanceState(outState );
        outState.putString(GalleryFragment.ALBUM,album);
    }


    private void showPopup(Context context) {
        loadAlbumList();
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_album_list, null);

        albumList = (ListView) layout.findViewById(R.id.album_list);
        popupWindow = new PopupWindow(
                layout,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                showGalleryFragment(titleAlbum.getText().toString());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gallery, menu);
        doneItem = menu.findItem(R.id.action_done);
        doneItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        doneSelection();
        return super.onOptionsItemSelected(item);
    }

    private void doneSelection(){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(GalleryFragment.TAG);
        if (fragment != null && fragment instanceof GalleryFragment) {
            Intent intent = new Intent();
            intent.putExtra(GalleryFragment.ALBUM,album);
            intent.putStringArrayListExtra(GalleryFragment.SELECTED_PHOTOS, ((GalleryFragment) fragment).getSelectedImage());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void initActionBar() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar_gallery);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("");
        mToolBar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag) {
                    finish();
                } else {
                    flag = false;
                    imageView.setVisibility(View.VISIBLE);
                    titleAlbum.setText(album);
                    doneItem.setVisible(false);
                    showGalleryFragment(album);
                }
            }
        });
        titleAlbum = (TextView) mToolBar.findViewById(R.id.album_name);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    private void loadAlbumList() {
        String[] projection = {MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                if (cursor != null && cursor.getCount() > 0) {
                    albumNames = new HashSet<>();
                    cursor.moveToFirst();
                    int bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                    String bucket;
                    String date;
                    long bucketId;
                    do {
                        bucket = cursor.getString(bucketColumn);
                        bucketId = cursor.getInt(bucketIdColumn);
                        albumNames.add(bucket);
                    } while (cursor.moveToNext());
                    ArrayList<String> arrayList = new ArrayList<>(albumNames);
                    itemsAdapter =
                            new AlbumNameAdapter(getApplicationContext(), arrayList);
                    albumList.setAdapter(itemsAdapter);
                    albumList.setDivider(null);
                    popupWindow.showAsDropDown(titleAlbum, 50, -30);
                }

            }
        };

        asyncQueryHandler.startQuery(1, null, images, projection, null, null, null);

    }

    @Override
    public void onSelect(int count) {
        if (count > 0) {
            flag = true;
            imageView.setVisibility(View.GONE);
            titleAlbum.setText(count + " Selected");
        } else {
            flag = false;
            imageView.setVisibility(View.VISIBLE);
            titleAlbum.setText(album);
        }
        if (doneItem != null )
            doneItem.setVisible(flag);

    }


    public class AlbumNameAdapter extends ArrayAdapter<String> {
        private ArrayList<String> data;

        public AlbumNameAdapter(Context context, ArrayList<String> arrayList) {
            super(context, 0, arrayList);
            data = new ArrayList<>();
            data.add(GalleryFragment.ALL_PHOTOS);
            data.addAll(arrayList);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_textview, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.txt_album_name);
            textView.setText(data.get(position));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    album = data.get(position);
                    titleAlbum.setText(album);
                    popupWindow.dismiss();
                }
            });

            return convertView;
        }

        @Override
        public int getCount() {
            return data.size();
        }
    }

}
