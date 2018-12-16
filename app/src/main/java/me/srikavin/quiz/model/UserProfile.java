package me.srikavin.quiz.model;

import android.media.Image;

import java.util.UUID;

public class UserProfile {
    private UUID uuid;
    private Image avatar;
    private String username;

    public UUID getUuid() {
        return uuid;
    }

    public Image getAvatar() {
        return avatar;
    }

    public String getUsername() {
        return username;
    }
}
