package me.srikavin.quiz.viewmodel;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class BattleViewModel extends ViewModel {
    private MutableLiveData<List<Quiz>> quizzes;

    public LiveData<List<Quiz>> getQuizzes() {
        if (quizzes == null) {
            quizzes = new MutableLiveData<>();
            updateQuizzes();
        }
        return quizzes;
    }

    public void updateQuizzes() {
        QuizRepository.INSTANCE.getQuizzes(new QuizRepository.QuizResponseHandler() {
            @Override
            public void handleMultiple(@Nullable List<Quiz> newQuizzes) {
                quizzes.postValue(newQuizzes);
            }
        });
    }
}