package me.srikavin.quiz.model;

import android.media.Image;

import androidx.annotation.Nullable;

public class QuizAnswer {
    boolean hasImage;
    @Nullable
    Image image;
    @Nullable
    String text;
    boolean correct;
}
