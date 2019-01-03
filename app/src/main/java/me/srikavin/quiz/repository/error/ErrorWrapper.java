package me.srikavin.quiz.repository.error;

public class ErrorWrapper<T, E extends Enum<E>> {
    private final T data;
    private final E[] errors;

    public ErrorWrapper(T data, E[] errors) {
        this.data = data;
        this.errors = errors;
    }

    public E[] getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && errors.length != 0;
    }

    public T getData() {
        return data;
    }
}
