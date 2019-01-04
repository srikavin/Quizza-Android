package me.srikavin.quiz.repository;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import androidx.annotation.NonNull;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;
import me.srikavin.quiz.model.AnswerResponse;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.model.QuizAnswer;
import me.srikavin.quiz.model.QuizGameState;
import me.srikavin.quiz.model.QuizQuestion;

import static me.srikavin.quiz.MainActivity.TAG;

public enum GameRepository {
    INSTANCE;

    private LocalGameRepository localGameRepository = new LocalGameRepository();

    public void createGame(Quiz quiz, GameResponseHandler handler) {
        localGameRepository.createGame(quiz, handler);
    }

    public void submitAnswer(String id, QuizAnswer answer) {
        localGameRepository.submitAnswer(id, answer);
    }

    public enum ErrorCodes {
        UNKNOWN_ERROR(0),
        NETWORK_ERROR(5),
        SERVER_ERROR(6);

        private int code;

        ErrorCodes(int code) {
            this.code = code;
        }

        static ErrorCodes fromCode(int errorCode) {
            for (ErrorCodes e : values()) {
                if (errorCode == e.code) {
                    return e;
                }
            }
            return UNKNOWN_ERROR;
        }
    }

    interface GameService {
        void createGame(Quiz quiz, GameResponseHandler handler);

        void submitAnswer(String id, QuizAnswer answer);
    }

    public abstract static class GameResponseHandler {
        public void handleAnswer(AnswerResponse response) {
            //By default, do nothing
        }

        public void handleQuestion(QuizQuestion question) {
            //By default, do nothing
        }

        public void handleGameCreate(String id, GameInfo info) {
            //By default, do nothing
        }

        public void handleGameStats(GameStats stats) {

        }

        public void handleScoreChange(int score) {

        }

        public void handleGameStateChange(QuizGameState state) {

        }

        public void handleGameTimeChange(int timeLeft) {

        }

        public void handleErrors(@NonNull ErrorCodes... errors) {
            //By default, print error codes
            for (GameRepository.ErrorCodes e : errors) {
                Log.w(TAG, "Ignored error code: " + e.name());
            }
        }
    }

    public static class GameStats {
        public final int correct;
        public final List<QuizAnswer> chosen;
        public final List<QuizQuestion> quizQuestions;
        public final double percentCorrect;

        public GameStats(int correct, List<QuizAnswer> chosen, List<QuizQuestion> quizQuestions) {
            this.correct = correct;
            this.chosen = chosen;
            this.quizQuestions = quizQuestions;
            percentCorrect = correct / (double) quizQuestions.size();
        }
    }

    public static class GameInfo {
        public final int timePerQuestion;
        public final int numberOfQuestions;

        public GameInfo(int timePerQuestion, int numberOfQuestions) {
            this.timePerQuestion = timePerQuestion;
            this.numberOfQuestions = numberOfQuestions;
        }
    }

    public static class LocalGameRepository implements GameService {
        private Map<String, Game> idGameMap = new HashMap<>();

        @Override
        public void createGame(Quiz quiz, GameResponseHandler handler) {
            String id = UUID.randomUUID().toString();
            Game game = new Game(id, quiz, handler);
            idGameMap.put(id, game);
            game.setState(QuizGameState.WAITING_FOR_PLAYERS);
            handler.handleGameCreate(id, new GameInfo(30, quiz.questions.size()));
            game.setState(QuizGameState.IN_PROGRESS);
            nextQuestion(game);
        }

        @Override
        public void submitAnswer(String id, QuizAnswer answer) {
            Game game = idGameMap.get(id);
            assert game != null;

            if (game.waitingForAnswer || game.state == QuizGameState.FINISHED) {
                return;
            }

            boolean correct = answer != null && answer.correct;

            game.chosen.add(answer);

            if (correct) {
                game.correct++;
                game.score += 150;
                game.handler.handleScoreChange(game.score);
            }

            QuizQuestion cur = game.quiz.questions.get(game.currentQuestion);
            List<QuizAnswer> correctAnswers = StreamSupport.stream(cur.answers)
                    .filter(e -> e.correct).collect(Collectors.toList());

            game.handler.handleAnswer(new AnswerResponse(correct, cur, correctAnswers));

            game.waitingForAnswer = true;
            // Delay the passage of the next question to allow for animations and effects
            game.countDownTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    nextQuestion(game);
                    game.waitingForAnswer = false;
                }
            }, 3500);
        }

        private void nextQuestion(Game game) {
            game.currentQuestion++;
            if (game.currentQuestion < game.total) {
                game.setCurrentQuestion(game.currentQuestion);
                QuizQuestion cur = game.quiz.questions.get(game.currentQuestion);
                Collections.shuffle(cur.answers);
                game.handler.handleQuestion(cur);
                if (game.executingTask != null) {
                    game.executingTask.cancel();
                }
                game.executingTask = new TimerTask() {
                    int seconds = 31;

                    @Override
                    public void run() {
                        seconds--;
                        game.handler.handleGameTimeChange(seconds);
                        if (seconds <= 0) {
                            submitAnswer(game.id, null);
                            cancel();
                        }
                    }
                };
                game.countDownTimer.scheduleAtFixedRate(game.executingTask, 0, 1000);
            } else {
                game.currentQuestion--;
                game.setState(QuizGameState.FINISHED);
                game.handler.handleGameStats(new GameStats(game.correct, game.chosen, game.quiz.questions));
                game.countDownTimer.cancel();
            }

        }

        class Game {
            Quiz quiz;
            String id;
            int total;
            int score;
            int currentQuestion = -1;
            int correct = 0;
            boolean waitingForAnswer = false;
            List<QuizAnswer> chosen = new ArrayList<>();
            GameResponseHandler handler;
            Timer countDownTimer = new Timer();
            TimerTask executingTask;
            private QuizGameState state;

            public Game(String id, Quiz quiz, GameResponseHandler handler) {
                this.id = id;
                this.handler = handler;
                this.quiz = quiz;
                Collections.shuffle(quiz.questions);
                total = quiz.questions.size();
            }

            void setState(QuizGameState state) {
                this.state = state;
                handler.handleGameStateChange(state);
            }

            void setCurrentQuestion(int currentQuestion) {
                this.currentQuestion = currentQuestion;
                state.setCurrentQuestion(currentQuestion);
                this.setState(state);
            }

        }
    }
}
