package me.srikavin.quiz.model;

public enum QuizGameState {
    WAITING_FOR_PLAYERS,
    IN_PROGRESS,
    FINISHED;

    int currentQuestion;

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
    }
}
