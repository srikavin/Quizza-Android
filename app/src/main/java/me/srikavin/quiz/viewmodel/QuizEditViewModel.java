package me.srikavin.quiz.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class QuizEditViewModel extends AndroidViewModel {
    private MutableLiveData<Quiz> quiz;
    private boolean creatingQuiz;
    private QuizRepository quizRepository;

    public QuizEditViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);
    }

    public LiveData<Quiz> createQuiz() {
        if (quiz == null) {
            quiz = new MutableLiveData<>();
            Quiz toSet = new Quiz();
            toSet.draft = true;
            quiz.postValue(toSet);
            creatingQuiz = true;
        }

        return quiz;
    }

    public LiveData<Quiz> editQuiz(String id) {
        if (quiz == null) {
            quiz = new MutableLiveData<>();
            creatingQuiz = false;
        }

        quizRepository.getQuizByID(id, new QuizRepository.QuizResponseHandler() {
            @Override
            public void handle(Quiz newQuiz) {
                quiz.postValue(newQuiz);
            }
        });

        return quiz;
    }

    public LiveData<Quiz> saveQuiz() {
        final MutableLiveData<Quiz> liveData = new MutableLiveData<>();
        Quiz quizVal = quiz.getValue();
        if (quizVal == null) {
            liveData.postValue(null);
            return liveData;
        }
        if (creatingQuiz) {
            quizRepository.createQuiz(quizVal, new QuizRepository.QuizResponseHandler() {
                @Override
                public void handle(Quiz quiz) {
                    liveData.postValue(quiz);
                }
            });
        } else {
            quizRepository.editQuiz(quizVal.id, quizVal, new QuizRepository.QuizResponseHandler() {
                @Override
                public void handle(Quiz quiz) {
                    liveData.postValue(quiz);
                }
            });
        }

        return liveData;
    }
}
