package me.srikavin.quiz.model

import com.google.gson.annotations.Expose
import me.srikavin.quiz.network.common.model.data.QuizModel
import me.srikavin.quiz.network.common.model.data.ResourceId
import java.util.*
import kotlin.collections.ArrayList

data class Quiz(
        @Expose(serialize = false)
        override var id: ResourceId = ResourceId(UUID.randomUUID().toString()),
        @Expose override var title: String = "",
        @Expose override var description: String = "",
        @Expose var tags: List<Tag> = ArrayList(),
        @Expose override var questions: MutableList<QuizQuestion> = mutableListOf(),
        @Expose var draft: Boolean = true,
        @Expose var overview: String = "",
        @Expose var coverImage: String? = null,
        var isLocal: Boolean = false
) : QuizModel