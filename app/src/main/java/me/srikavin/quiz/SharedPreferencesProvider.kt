package me.srikavin.quiz

import android.content.Context
import android.content.SharedPreferences
import me.srikavin.quiz.repository.QuizRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module

val appModule = module {
    single<SharedPreferences> {
        androidApplication().getSharedPreferences("savedQuizzes", Context.MODE_PRIVATE)
    }

    single { QuizRepository(get()) }
}
