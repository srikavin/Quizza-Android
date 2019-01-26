package me.srikavin.quiz.repository.error

class ErrorWrapper<T, out E : Enum<out E>>(val data: T?, val errors: Array<out E>?) {
    fun hasErrors(): Boolean {
        return errors != null && errors.isNotEmpty()
    }
}
