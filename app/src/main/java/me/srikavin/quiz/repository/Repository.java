package me.srikavin.quiz.repository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public abstract class Repository {
    protected abstract static class ResponseHandler<E, T> {
        @WorkerThread
        public abstract void handle(@Nullable T t);

        @WorkerThread
        public abstract void handleMultiple(@Nullable List<T> t);

        @WorkerThread
        public abstract void handleErrors(@NonNull E[] errors);
    }
}
