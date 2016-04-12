package com.example.rebeccastecker.quizletquest.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.rebeccastecker.quizletquest.GameMaster;

/**
 * Created by rebeccastecker on 4/11/16.
 */
public class Grade implements Parcelable{
    public final boolean isCorrect;
    public final String correctText;
    public final Img correctImg;
    public final String submittedText;
    public final Img submittedImg;
    public final String questionText;
    public final Img questionImg;
    public final String wouldHaveBeenText;
    public final Img wouldHaveBeenImg;
    public final boolean sawDefinition;

    public Grade(boolean isCorrect, boolean sawDefinition, String correctText, Img correctImg, String submittedText, Img submittedImg, String questionText, Img questionImg, String wouldHaveBeenText, Img wouldHaveBeenImg) {
        this.isCorrect = isCorrect;
        this.sawDefinition = sawDefinition;
        this.correctText = correctText;
        this.correctImg = correctImg;
        this.submittedText = submittedText;
        this.submittedImg = submittedImg;
        this.questionText = questionText;
        this.questionImg = questionImg;
        this.wouldHaveBeenText = wouldHaveBeenText;
        this.wouldHaveBeenImg = wouldHaveBeenImg;
    }

    protected Grade(Parcel in) {
        isCorrect = in.readByte() != 0;
        correctText = in.readString();
        correctImg = in.readParcelable(Img.class.getClassLoader());
        submittedText = in.readString();
        submittedImg = in.readParcelable(Img.class.getClassLoader());
        questionText = in.readString();
        questionImg = in.readParcelable(Img.class.getClassLoader());
        wouldHaveBeenText = in.readString();
        wouldHaveBeenImg = in.readParcelable(Img.class.getClassLoader());
        sawDefinition = in.readByte() != 0;
    }

    public static final Creator<Grade> CREATOR = new Creator<Grade>() {
        @Override
        public Grade createFromParcel(Parcel in) {
            return new Grade(in);
        }

        @Override
        public Grade[] newArray(int size) {
            return new Grade[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isCorrect ? 1 : 0));
        dest.writeString(correctText);
        dest.writeParcelable(correctImg, flags);
        dest.writeString(submittedText);
        dest.writeParcelable(submittedImg, flags);
        dest.writeString(questionText);
        dest.writeParcelable(questionImg, flags);
        dest.writeString(wouldHaveBeenText);
        dest.writeParcelable(wouldHaveBeenImg, flags);
        dest.writeByte((byte) (sawDefinition ? 1 : 0));
    }
}
