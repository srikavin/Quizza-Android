package me.srikavin.quiz.model;

import android.media.Image;

import com.google.gson.annotations.Expose;

public class UserProfile {
    @Expose
    private String id;
    @Expose
    private Image avatar;
    @Expose
    private String username;

    public Image getAvatar() {
        return avatar;
    }

    public String getUsername() {
        return username;
    }
}
