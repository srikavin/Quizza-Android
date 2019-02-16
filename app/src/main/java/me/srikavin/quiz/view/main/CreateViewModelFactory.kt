package me.srikavin.quiz.view.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.srikavin.quiz.viewmodel.CreateViewModel

class CreateViewModelFactory(private val battleFragment: CreateFragment) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreateViewModel(null) as T
    }

}