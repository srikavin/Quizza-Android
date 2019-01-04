package me.srikavin.quiz.model;

import android.media.Image;

import com.google.gson.annotations.Expose;

import java.util.Objects;

import androidx.annotation.Nullable;

public class QuizAnswer {
    @Expose
    public String id;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizAnswer that = (QuizAnswer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
