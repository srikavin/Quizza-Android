package me.srikavin.quiz.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import me.srikavin.quiz.model.AnswerResponse;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.model.QuizAnswer;
import me.srikavin.quiz.model.QuizGameState;
import me.srikavin.quiz.model.QuizQuestion;
import me.srikavin.quiz.repository.GameRepository;
import me.srikavin.quiz.repository.QuizRepository;

public class GameViewModel extends ViewModel {
    private MutableLiveData<Quiz> quiz;

    private MutableLiveData<Integer> gameScore = new MutableLiveData<>();

    private MutableLiveData<String> currentGameID = new MutableLiveData<>();
    private MutableLiveData<GameRepository.GameInfo> gameInfo = new MutableLiveData<>();
    private MutableLiveData<QuizQuestion> currentQuestion = new MutableLiveData<>();
    private MutableLiveData<AnswerResponse> answerResponse = new MutableLiveData<>();
    private MutableLiveData<GameRepository.GameStats> gameStats = new MutableLiveData<>();
    private MutableLiveData<QuizGameState> gameState = new MutableLiveData<>();
    private MutableLiveData<Integer> timeRemaining = new MutableLiveData<>();
    private MutableLiveData<Void> createGame;


    public LiveData<String> getCurrentGameID() {
        return currentGameID;
    }

    public LiveData<GameRepository.GameInfo> getGameInfo() {
        return gameInfo;
    }

    public LiveData<QuizGameState> getGameState() {
        return gameState;
    }

    public LiveData<Integer> getTimeRemaining() {
        return timeRemaining;
    }

    public LiveData<QuizQuestion> getCurrentQuestion() {
        return currentQuestion;
    }

    public LiveData<AnswerResponse> getAnswerResponse() {
        return answerResponse;
    }

    public LiveData<GameRepository.GameStats> getGameStats() {
        return gameStats;
    }

    public LiveData<Integer> getGameScore() {
        return gameScore;
    }

    public LiveData<Quiz> getQuizByID(String id) {
        if (quiz == null) {
            quiz = new MutableLiveData<>();
            loadQuizzes(id);
        }
        return quiz;
    }

    public void createGame(Quiz quiz) {
        if (createGame == null) {
            createGame = new MutableLiveData<>();
            GameRepository.INSTANCE.createGame(quiz, new GameRepository.GameResponseHandler() {
                @Override
                public void handleAnswer(AnswerResponse response) {
                    answerResponse.postValue(response);
                }

                @Override
                public void handleQuestion(QuizQuestion question) {
                    currentQuestion.postValue(question);
                }

                @Override
                public void handleGameStateChange(QuizGameState state) {
                    gameState.postValue(state);
                }

                @Override
                public void handleScoreChange(int score) {
                    gameScore.postValue(score);
                }

                @Override
                public void handleGameCreate(String id, GameRepository.GameInfo info) {
                    currentGameID.postValue(id);
                    gameInfo.postValue(info);
                }

                @Override
                public void handleGameStats(GameRepository.GameStats stats) {
                    gameStats.postValue(stats);
                }

                @Override
                public void handleGameTimeChange(int timeLeft) {
                    timeRemaining.postValue(timeLeft);

                }
            });
        }
        return;
    }

    public void submitAnswer(QuizAnswer quizAnswer) {
        GameRepository.INSTANCE.submitAnswer(currentGameID.getValue(), quizAnswer);
    }

    private void loadQuizzes(String id) {
        QuizRepository.INSTANCE.getQuizByID(id, new QuizRepository.QuizResponseHandler() {
            @Override
            public void handle(Quiz quiz) {
                GameViewModel.this.quiz.postValue(quiz);
            }
        });
    }
}
