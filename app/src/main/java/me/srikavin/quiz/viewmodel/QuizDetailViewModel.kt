package me.srikavin.quiz.viewmodel

import android.content.Context
import com.ww.roxie.BaseAction
import com.ww.roxie.BaseState
import com.ww.roxie.BaseViewModel
import com.ww.roxie.Reducer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.QuizRepository

sealed class QuizDetailAction : BaseAction {
    data class LoadQuiz(val quizId: String) : QuizDetailAction()
}

sealed class QuizDetailChange {
    object Loading : QuizDetailChange()
    data class QuizLoad(val quiz: Quiz) : QuizDetailChange()
    data class Error(val error: Throwable?) : QuizDetailChange()
}

data class QuizDetailState(
        val quiz: Quiz? = null,
        val loading: Boolean = false,
        val error: Boolean = false
) : BaseState

class QuizDetailViewModel(initialState: QuizDetailState?, context: Context) : BaseViewModel<QuizDetailAction, QuizDetailState>() {
    private val quizRepository: QuizRepository = QuizRepository(context)
    override val initialState = initialState ?: QuizDetailState()

    private val reducer: Reducer<QuizDetailState, QuizDetailChange> = { state, change ->
        when (change) {
            is QuizDetailChange.Loading -> state.copy(
                    loading = true,
                    error = false
            )

            is QuizDetailChange.QuizLoad -> state.copy(
                    quiz = change.quiz,
                    loading = false,
                    error = false
            )

            is QuizDetailChange.Error -> state.copy(
                    error = true,
                    loading = false
            )
        }
    }

    init {
        bindActions()
    }

    private fun bindActions() {
        val quizDetailChanges = actions.ofType<QuizDetailAction.LoadQuiz>()
                .switchMap { action ->
                    quizRepository
                            .getQuizByID(action.quizId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .toObservable()
                            .map<QuizDetailChange> { QuizDetailChange.QuizLoad(it) }
                            .onErrorReturn { QuizDetailChange.Error(it) }
                            .startWith(QuizDetailChange.Loading)
                }

        disposables += quizDetailChanges
                .scan(initialState, reducer)
                .distinctUntilChanged()
                .subscribe(state::postValue)
    }
}
