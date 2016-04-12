package com.example.rebeccastecker.quizletquest;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import com.example.rebeccastecker.quizletquest.models.Grade;
import com.example.rebeccastecker.quizletquest.models.Img;
import com.example.rebeccastecker.quizletquest.models.Set;
import com.example.rebeccastecker.quizletquest.models.Term;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rebeccastecker on 4/11/16.
 */
public class GameMaster implements Parcelable {
    protected GameMaster(Parcel in) {
        set = in.readParcelable(Set.class.getClassLoader());
        missedTerms = in.createTypedArrayList(Grade.CREATOR);
        upcomingQuestions = in.createTypedArrayList(Term.CREATOR);
    }

    public static final Creator<GameMaster> CREATOR = new Creator<GameMaster>() {
        @Override
        public GameMaster createFromParcel(Parcel in) {
            return new GameMaster(in);
        }

        @Override
        public GameMaster[] newArray(int size) {
            return new GameMaster[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(set, flags);
        dest.writeTypedList(missedTerms);
        dest.writeTypedList(upcomingQuestions);
    }

    public enum Mode {
        TERMS,
        DEFINITIONS,
        MIX
    }
    private Set set;
    private List<Grade> missedTerms;
    private List<Term> upcomingQuestions;
    private Mode mode;
    private Mode currentlyShowing;


    public GameMaster(Set set, Mode mode) {
        this.set = set;
        this.mode = mode;
        init();
    }

    private void init() {
        missedTerms = new ArrayList<>();
        restartQuest();
    }
    public boolean isOver() {
        return upcomingQuestions.size() == 0;
    }
    public String getCurrentQuestionText() {
        if (currentlyShowing == Mode.TERMS) {
            return upcomingQuestions.get(0).term;
        }
        return upcomingQuestions.get(0).def;
    }
    public Img getCurrentQuestionImage() {
        if (currentlyShowing == Mode.TERMS) {
            return null;
        }
        return upcomingQuestions.get(0).image;
    }

    public double getProgressPercentage() {
        return (1.0 * set.terms.size() - upcomingQuestions.size()) / set.terms.size();
    }

    public Grade submitAnswer(long termId) {
        // TODO : handle when there's a term/definition that appears more than once
        Term currentTerm = currentTerm();
        String correctText = (currentlyShowing == Mode.TERMS) ? currentTerm.def : currentTerm.term;
        Img correctImg = (currentlyShowing == Mode.TERMS) ? currentTerm.image : null;
        String questionText = (currentlyShowing == Mode.TERMS) ? currentTerm.term : currentTerm.def;
        Img questionImg = (currentlyShowing == Mode.TERMS) ? null : currentTerm.image;

        boolean isCorrect = currentTerm.id == termId;
        Term theyGuessed = findTermById(termId);

        // FIXME : handle better if they submit an invalid termID. Do we just not even grade it?
        String submittedText = "-- missing --";
        Img submittedImg = null;
        String wouldHaveBeenText = "-- missing --";
        Img wouldHaveBeenImg = null;

        if (!isCorrect && theyGuessed != null) {
            submittedText = (currentlyShowing == Mode.TERMS) ? theyGuessed.def : theyGuessed.term;
            submittedImg = (currentlyShowing == Mode.TERMS) ? theyGuessed.image : null;
            wouldHaveBeenText = (currentlyShowing == Mode.TERMS) ? theyGuessed.term : theyGuessed.def;
            wouldHaveBeenImg = (currentlyShowing == Mode.TERMS) ? null : theyGuessed.image;
        }

        Grade g = new Grade(isCorrect, currentlyShowing == Mode.DEFINITIONS, correctText, correctImg, submittedText, submittedImg, questionText, questionImg, wouldHaveBeenText, wouldHaveBeenImg);

        if (!isCorrect) {
            missedTerms.add(g);
        }
        return g;
    }

    /**
     * @return if there is a valid question (false if they've reached the end)
     */
    public boolean nextQuestion() {
        upcomingQuestions.remove(0);
        if (mode == Mode.MIX) {
            currentlyShowing = (Math.random() > 0.5) ? Mode.DEFINITIONS : Mode.TERMS;
        }
        return !isOver();
    }

    public void restartQuest() {
        upcomingQuestions = new ArrayList<>(set.terms);
        Collections.shuffle(upcomingQuestions);

        currentlyShowing = mode;
        if (mode == Mode.MIX) {
            currentlyShowing = (Math.random() > 0.5) ? Mode.DEFINITIONS : Mode.TERMS;
        }
    }

    private Term findTermById(long termId) {
        for (Term t : set.terms) {
            if (t.id == termId) {
                return t;
            }
        }
        return null;
    }

    private Term currentTerm() {
        return upcomingQuestions.get(0);
    }

}
