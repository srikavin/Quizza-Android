package me.srikavin.quiz.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class BattleViewModel extends AndroidViewModel {
    private final QuizRepository quizRepository;
    private MutableLiveData<List<Quiz>> quizzes;

    public BattleViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);
    }

    public LiveData<List<Quiz>> getQuizzes() {
        if (quizzes == null) {
            quizzes = new MutableLiveData<>();
            updateQuizzes();
        }
        return quizzes;
    }

    public void updateQuizzes() {
        quizRepository.getQuizzes(new QuizRepository.QuizResponseHandler() {
            @Override
            public void handleMultiple(@Nullable List<? extends Quiz> newQuizzes) {
                quizzes.postValue((List<Quiz>) newQuizzes);
            }
        });
    }
}