package me.srikavin.quiz.model;

import android.media.Image;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class QuizQuestion {
    @Nullable
    public Image image;

    @Expose
    public String text;

    @Expose
    public List<QuizAnswer> answers;

    public QuizQuestion() {
        text = "";
        answers = new ArrayList<>();
    }
}
