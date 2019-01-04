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

public class CreateViewModel extends AndroidViewModel {
    private MutableLiveData<List<Quiz>> ownedQuizzes;

    public CreateViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Quiz>> getOwnedQuizzes() {
        if (ownedQuizzes == null) {
            ownedQuizzes = new MutableLiveData<>();
            updateDrafts();
        }
        return ownedQuizzes;
    }

    public LiveData<List<Quiz>> deleteQuiz(Quiz quiz) {
        QuizRepository.INSTANCE.deleteQuiz(quiz, new QuizRepository.QuizResponseHandler() {
            @Override
            public void handle(Quiz user) {
                updateDrafts();
            }

            @Override
            public void handleErrors(@NonNull QuizRepository.ErrorCodes... errors) {
                super.handleErrors(errors);
            }
        });
        return ownedQuizzes;
    }

    public void updateDrafts() {
        QuizRepository.INSTANCE.getOwned(getApplication(), new QuizRepository.QuizResponseHandler() {
            @Override
            public void handleMultiple(@Nullable List<Quiz> newQuizzes) {
                ownedQuizzes.postValue(newQuizzes);
            }
        });
    }
}
