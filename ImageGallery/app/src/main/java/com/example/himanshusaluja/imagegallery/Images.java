package com.example.himanshusaluja.imagegallery;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by himanshu.saluja on 27/08/16.
 */
public class Images implements Parcelable{
    public long id;
    public String name;
    public String path;
    public boolean isSelected;

    public Images(long id, String name, String path, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.isSelected = isSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(path);
    }

    public static final Parcelable.Creator<Images> CREATOR = new Parcelable.Creator<Images>() {
        @Override
        public Images createFromParcel(Parcel source) {
            return new Images(source);
        }

        @Override
        public Images[] newArray(int size) {
            return new Images[size];
        }
    };

    private Images(Parcel in) {
        id = in.readLong();
        name = in.readString();
        path = in.readString();
    }
}
