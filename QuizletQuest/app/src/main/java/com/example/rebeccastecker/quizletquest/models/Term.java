package com.example.rebeccastecker.quizletquest.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by rebeccastecker on 4/11/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Term implements Parcelable {
    public String term;
    public Long id;
    @JsonProperty("definition")
    public String def;
    public Img image;

    public Term() {}
    
    protected Term(Parcel in) {
        term = in.readString();
        def = in.readString();
        image = in.readParcelable(Img.class.getClassLoader());
    }

    public static final Creator<Term> CREATOR = new Creator<Term>() {
        @Override
        public Term createFromParcel(Parcel in) {
            return new Term(in);
        }

        @Override
        public Term[] newArray(int size) {
            return new Term[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(term);
        dest.writeString(def);
        dest.writeParcelable(image, flags);
    }
}
