package me.srikavin.quiz.repository.error

class ErrorWrapper<T, E : Enum<E>>(val data: T, val errors: Array<E>?) {
    fun hasErrors(): Boolean {
        return errors != null && errors.isNotEmpty()
    }
}
