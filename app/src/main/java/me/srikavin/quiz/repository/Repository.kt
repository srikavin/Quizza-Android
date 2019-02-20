package me.srikavin.quiz.repository

import androidx.annotation.WorkerThread

/**
 * Used to fetch data from local or online sources
 */
abstract class Repository {
    /**
     * Used to handle callbacks for repository events
     */
    abstract class ResponseHandler<E, T> {
        /**
         * Handles a result providing a single item.
         *
         * @param t May be null. Contains the specified object
         */
        @WorkerThread
        abstract fun handle(t: T?)

        /**
         * Handles a result providing multiple items
         */
        @WorkerThread
        abstract fun handleMultiple(t: List<T>)

        /**
         * Handles an error that occurred as a result of calling a repository function
         */
        @WorkerThread
        abstract fun handleErrors(vararg errors: E)
    }
}
