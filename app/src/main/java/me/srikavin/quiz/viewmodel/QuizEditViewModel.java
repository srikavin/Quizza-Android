package me.srikavin.quiz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.repository.QuizRepository;

public class QuizEditViewModel extends ViewModel {
    private Quiz quiz;

    public Quiz createQuiz() {
        if (quiz == null) {
            quiz = new Quiz();
        }
        return quiz;
    }

    public Quiz editQuiz() {
        //TODO
        return quiz;
    }

    public LiveData<Quiz> saveQuiz() {
        final MutableLiveData<Quiz> liveData = new MutableLiveData<>();
        quiz.draft = true;
        QuizRepository.INSTANCE.createQuiz(quiz, new QuizRepository.QuizResponseHandler() {
            @Override
            public void handle(Quiz quiz) {
                liveData.postValue(quiz);
            }
        });

        return liveData;
    }
}
