package me.srikavin.quiz.view.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.srikavin.quiz.viewmodel.QuizDetailViewModel

class QuizDetailViewModelFactory(private val quizDetailFragment: QuizDetailFragment) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return QuizDetailViewModel(null, quizDetailFragment.context!!) as T
    }

}