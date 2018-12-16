package me.srikavin.quiz.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.srikavin.quiz.R;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.viewmodel.QuizViewModel;

public class QuizFragment extends Fragment {

    private QuizViewModel mViewModel;

    public static QuizFragment newInstance() {
        return new QuizFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quiz_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(QuizViewModel.class);
        System.out.println(1);
        mViewModel.getQuizzes().observe(this, new Observer<List<Quiz>>() {
            @Override
            public void onChanged(@Nullable List<Quiz> quizzes) {
                System.out.println(2);
                if (quizzes != null) {
                    System.out.println("SUCCESS " + quizzes.size());
                    for (Quiz e : quizzes) {
                        System.out.println("SUCCESS");
                        System.out.println(e.title);
                    }
                    //Loaded; update view
                }
                System.out.println(3);
            }
        });
    }

}
