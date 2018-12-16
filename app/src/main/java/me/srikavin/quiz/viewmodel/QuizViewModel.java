package me.srikavin.quiz.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.List;

import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class QuizViewModel extends ViewModel {
    private MediatorLiveData<List<Quiz>> quizzes;
    private LiveData<List<Quiz>> dataSource;

    public LiveData<List<Quiz>> getQuizzes() {
        if (quizzes == null) {
            quizzes = new MediatorLiveData<>();
            loadQuizzes();
        }
        return quizzes;
    }

    private void loadQuizzes() {
        if (dataSource != null) {
            quizzes.removeSource(dataSource);
        }
        dataSource = QuizRepository.INSTANCE.getQuizzes();

        quizzes.addSource(dataSource, new Observer<List<Quiz>>() {
            @Override
            public void onChanged(@Nullable List<Quiz> changedQuizzes) {
                quizzes.setValue(changedQuizzes);
            }
        });

    }
}
