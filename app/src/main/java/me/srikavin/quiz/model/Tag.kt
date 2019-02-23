package me.srikavin.quiz.model

import com.google.gson.annotations.Expose

/**
 * A data class that represents the metadata of a quiz, such as topics and categories.
 */
class Tag {
    /**
     * The name of a given topic or category
     */
    @Expose
    var name: String? = null
}
