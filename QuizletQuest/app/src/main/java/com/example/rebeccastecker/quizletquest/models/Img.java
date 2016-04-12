package com.example.rebeccastecker.quizletquest.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by rebeccastecker on 4/11/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Img implements Parcelable{
    public Integer height;
    public String url;
    public Integer width;

    public Img() {}

    protected Img(Parcel in) {
        url = in.readString();
    }

    public static final Creator<Img> CREATOR = new Creator<Img>() {
        @Override
        public Img createFromParcel(Parcel in) {
            return new Img(in);
        }

        @Override
        public Img[] newArray(int size) {
            return new Img[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }
}
