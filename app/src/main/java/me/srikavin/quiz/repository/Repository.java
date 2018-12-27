package me.srikavin.quiz.repository;

import java.util.List;

import androidx.annotation.WorkerThread;

public abstract class Repository {
    protected abstract static class ResponseHandler<E, T> {
        @WorkerThread
        public abstract void handle(T t);

        @WorkerThread
        public abstract void handleMultiple(List<T> t);

        @WorkerThread
        public abstract void handleErrors(E[] errors);
    }
}
