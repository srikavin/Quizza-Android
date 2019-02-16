package me.srikavin.quiz.view.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.srikavin.quiz.viewmodel.BattleViewModel

class BattleListModelFactory(private val battleFragment: BattleFragment) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BattleViewModel(null) as T
    }

}