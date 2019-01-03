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

    public String coverImageUrl;

    public Quiz() {
        this.id = UUID.randomUUID().toString();
        this.tags = new ArrayList<>();
        this.description = "";
        this.questions = new ArrayList<>();
        this.draft = true;
        this.overview = "";
        this.title = "";
    }
}
