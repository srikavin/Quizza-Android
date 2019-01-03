package me.srikavin.quiz.viewmodel;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class QuizDetailViewModel extends ViewModel {

    private Map<String, MutableLiveData<Quiz>> quizzes = new HashMap<>();

    public LiveData<Quiz> getQuizByID(String id) {
        if (!quizzes.containsKey(id)) {
            quizzes.put(id, new MutableLiveData<Quiz>());
            updateQuiz(id);
        }
        return quizzes.get(id);
    }

    public void updateQuiz(final String id) {
        QuizRepository.INSTANCE.getQuizByID(id, new QuizRepository.QuizResponseHandler() {
            @Override
            public void handle(@Nullable Quiz newQuiz) {
                quizzes.get(id).postValue(newQuiz);
            }
        });
    }
}
