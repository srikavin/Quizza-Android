package me.srikavin.quiz.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Quiz {
    @Expose(serialize = false)
    public String id;
    @Expose
    public String title;
    @Expose
    public String description;
    @Expose
    public List<Tag> tags;
    @Expose
    public List<QuizQuestion> questions;
    @Expose
    public boolean draft;
    @Expose
    public String overview;
    @Expose
    public String coverImage;

    public boolean isLocal = false;


    public Quiz() {
        this.id = UUID.randomUUID().toString();
        this.tags = new ArrayList<>();
        this.description = "";
        this.questions = new ArrayList<>();
        this.draft = true;
        this.overview = "";
        this.title = "";
    }

    public Quiz(Quiz quiz) {
        this.id = quiz.id;
        this.title = quiz.title;
        this.description = quiz.description;
        this.tags = quiz.tags;
        this.questions = quiz.questions;
        this.draft = quiz.draft;
        this.overview = quiz.overview;
        this.coverImage = quiz.coverImage;
    }
}
