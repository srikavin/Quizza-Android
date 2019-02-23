package me.srikavin.quiz.repository.error

/**
 * A wrapper for errors that allows for data and a list of errors to be contained in a type-safe
 * way. If [hasErrors] returns true, the [data] value will be null.
 */
class ErrorWrapper<T, out E : Enum<out E>>(
        /**
         * The data contained with this class. Should only be null if errors is present.
         */
        val data: T?,
        /**
         * The errors associated with computing [data]. Should be null if no errors are present.
         */
        val errors: Array<out E>?
) {
    /**
     * Returns whether or not any errors occurred.
     *
     * @return If this returns true, [data] will be null. If this returns false, [errors] will be null.
     */
    fun hasErrors(): Boolean {
        return errors != null && errors.isNotEmpty()
    }
}
