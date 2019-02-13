package me.srikavin.quiz.model

import com.google.gson.annotations.Expose
import java.util.*


class Quiz {
    @Expose(serialize = false)
    var id: String
    @Expose
    var title: String
    @Expose
    var description: String
    @Expose
    var tags: List<Tag>
    @Expose
    var questions: MutableList<QuizQuestion>
    @Expose
    var draft: Boolean = false
    @Expose
    var overview: String
    @Expose
    var coverImage: String? = null

    var isLocal = false


    constructor() {
        this.id = UUID.randomUUID().toString()
        this.tags = ArrayList()
        this.description = ""
        this.questions = ArrayList()
        this.draft = true
        this.overview = ""
        this.title = ""
    }

    constructor(quiz: Quiz) {
        this.id = quiz.id
        this.title = quiz.title
        this.description = quiz.description
        this.tags = quiz.tags
        this.questions = quiz.questions
        this.draft = quiz.draft
        this.overview = quiz.overview
        this.coverImage = quiz.coverImage
    }
}
