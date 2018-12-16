package me.srikavin.quiz.model;

import android.media.Image;
import android.support.annotation.Nullable;

public class QuizAnswer {
    boolean hasImage;
    @Nullable
    Image image;
    @Nullable
    String text;
    boolean correct;
}
