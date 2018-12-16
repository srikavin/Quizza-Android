package me.srikavin.quiz.model;

import android.media.Image;

import java.util.List;

import androidx.annotation.Nullable;

public class QuizQuestion {
    boolean hasImage;
    @Nullable
    Image image;
    String question;
    List<QuizAnswer> answers;
}
