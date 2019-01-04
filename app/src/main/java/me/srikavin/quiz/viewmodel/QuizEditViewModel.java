package me.srikavin.quiz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class QuizEditViewModel extends ViewModel {
    private MutableLiveData<Quiz> quiz;
    private boolean creatingQuiz;

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

        QuizRepository.INSTANCE.getQuizByID(id, new QuizRepository.QuizResponseHandler() {
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
            QuizRepository.INSTANCE.createQuiz(quizVal, new QuizRepository.QuizResponseHandler() {
                @Override
                public void handle(Quiz quiz) {
                    liveData.postValue(quiz);
                }
            });
        } else {
            QuizRepository.INSTANCE.editQuiz(quizVal.id, quizVal, new QuizRepository.QuizResponseHandler() {
                @Override
                public void handle(Quiz quiz) {
                    liveData.postValue(quiz);
                }
            });
        }

        return liveData;
    }
}
