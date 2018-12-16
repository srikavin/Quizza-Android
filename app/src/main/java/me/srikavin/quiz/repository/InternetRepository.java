package me.srikavin.quiz.repository;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class InternetRepository {
    protected Retrofit retrofit;

    public InternetRepository() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.7:4000/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
