package me.srikavin.quiz.model

/**
 * An enum representing the possible states a game can be in
 */
enum class QuizGameState {
    /**
     * Indicates that the game has not yet started. It is waiting for players or delayed for any
     * reason
     */
    WAITING,
    /**
     * Indicates that the game is currently in progress and is being updated actively
     */
    IN_PROGRESS,
    /**
     * Indicates that the game is over and no more updates will be issued to any game handlers
     */
    FINISHED;

    /**
     * The current question the game is on
     */
    internal var currentQuestion: Int = 0
}
