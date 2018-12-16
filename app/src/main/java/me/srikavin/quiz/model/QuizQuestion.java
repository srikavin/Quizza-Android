package me.srikavin.quiz.model;

import android.media.Image;
import android.support.annotation.Nullable;

import java.util.List;

public class QuizQuestion {
    boolean hasImage;
    @Nullable
    Image image;
    String question;
    List<QuizAnswer> answers;
}
