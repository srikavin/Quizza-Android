package me.srikavin.quiz.view.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.srikavin.quiz.viewmodel.ProfileViewModel

internal class ProfileViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(null) as T
    }
}