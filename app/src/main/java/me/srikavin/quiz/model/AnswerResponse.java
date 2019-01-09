package me.srikavin.quiz.model;

import java.util.List;

/**
 * A Model class that holds the structure of an answer response as returned by a repository
 */
public class AnswerResponse {
    private boolean correct;
    private QuizQuestion question;
    private List<QuizAnswer> correctAnswers;


    public AnswerResponse(boolean correct, QuizQuestion question, List<QuizAnswer> correctAnswers) {
        this.correct = correct;
        this.question = question;
        this.correctAnswers = correctAnswers;
    }

    public boolean isCorrect() {
        return correct;
    }

    public List<QuizAnswer> getCorrectAnswers() {
        return correctAnswers;
    }
}
