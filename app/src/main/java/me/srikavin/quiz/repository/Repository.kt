package me.srikavin.quiz.repository

import androidx.annotation.WorkerThread

abstract class Repository {
    abstract class ResponseHandler<E, T> {
        @WorkerThread
        abstract fun handle(t: T?)

        @WorkerThread
        abstract fun handleMultiple(t: List<T>?)

        @WorkerThread
        abstract fun handleErrors(vararg errors: E)
    }
}
