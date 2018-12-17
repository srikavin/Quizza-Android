package me.srikavin.quiz.viewmodel;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class BattleViewModel extends ViewModel {
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
                System.out.println("CHANGED");
            }
        });

    }
}