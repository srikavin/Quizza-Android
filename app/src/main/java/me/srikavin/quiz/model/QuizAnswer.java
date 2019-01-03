package me.srikavin.quiz.model;

import android.media.Image;

import com.google.gson.annotations.Expose;

import androidx.annotation.Nullable;

public class QuizAnswer {
    @Nullable
    @Expose
    public Image image;
    @Expose
    public String text;
    @Expose
    public boolean correct;

    public QuizAnswer() {
        this.text = "";
        this.correct = false;
    }
}
