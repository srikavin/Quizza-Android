package me.srikavin.quiz.model

import com.google.gson.annotations.Expose
import me.srikavin.quiz.network.common.model.data.ResourceId

data class QuizAuthor(@Expose val id: ResourceId, @Expose val username: String)