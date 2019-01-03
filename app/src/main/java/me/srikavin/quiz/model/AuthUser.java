package me.srikavin.quiz.model;

import com.google.gson.annotations.Expose;

public class AuthUser {
    @Expose
    private boolean auth;
    @Expose
    private String token;


    public String getToken() {
        return token;
    }

    public boolean isAuth() {
        return auth;
    }

}
