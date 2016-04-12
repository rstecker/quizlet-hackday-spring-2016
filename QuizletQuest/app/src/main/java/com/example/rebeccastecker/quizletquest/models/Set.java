package com.example.rebeccastecker.quizletquest.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by rebeccastecker on 4/11/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Set implements Parcelable {
    @JsonProperty("created_by")
    public String username;
    public Long id;
    public String title;
    public List<Term> terms;

    public Set() {}

    protected Set(Parcel in) {
        username = in.readString();
        title = in.readString();
        terms = in.createTypedArrayList(Term.CREATOR);
    }

    public static final Creator<Set> CREATOR = new Creator<Set>() {
        @Override
        public Set createFromParcel(Parcel in) {
            return new Set(in);
        }

        @Override
        public Set[] newArray(int size) {
            return new Set[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(title);
        dest.writeTypedList(terms);
    }
}
