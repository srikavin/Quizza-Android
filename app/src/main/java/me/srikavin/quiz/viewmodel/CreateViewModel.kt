package me.srikavin.quiz.viewmodel

import android.content.Context
import com.ww.roxie.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.QuizRepository

sealed class CreateQuizAction : BaseAction {
    object Load : CreateQuizAction()
    object Refresh : CreateQuizAction()
    data class DeleteQuiz(val quiz: Quiz) : CreateQuizAction()
    data class ToggleExpansion(val quiz: Quiz) : CreateQuizAction()
}

sealed class CreateQuizChange {
    object Loading : CreateQuizChange()
    data class FinishLoad(val quizzes: List<Quiz>) : CreateQuizChange()
    data class Error(val error: Throwable?) : CreateQuizChange()
    data class ToggleExpansion(val quiz: Quiz) : CreateQuizChange()
}

data class CreateQuizState(
        val quizzes: List<Quiz> = emptyList(),
        val loading: Boolean = false,
        val error: Boolean = false,
        val expanded: Set<Quiz> = HashSet()
) : BaseState

class CreateViewModel(initialState: CreateQuizState?, context: Context) : BaseViewModel<CreateQuizAction, CreateQuizState>() {
    override val initialState = initialState ?: CreateQuizState()
    private val quizRepository: QuizRepository = QuizRepository(context)

    private val reducer: Reducer<CreateQuizState, CreateQuizChange> = { state, change ->
        when (change) {
            is CreateQuizChange.Loading -> state.copy(
                    loading = true,
                    error = false
            )

            is CreateQuizChange.FinishLoad -> state.copy(
                    quizzes = change.quizzes,
                    expanded = HashSet(),
                    loading = false,
                    error = false
            )

            is CreateQuizChange.Error -> state.copy(
                    error = true,
                    loading = false
            )

            is CreateQuizChange.ToggleExpansion -> {
                println(state)
                println(change)
                println(state.expanded)
                state.copy(
                        expanded = state.expanded.toggle(change.quiz)
                )
            }
        }
    }


    init {
        bindActions()
        Roxie.enableLogging()
    }

    private fun bindActions() {
        val loadQuizzes = {
            quizRepository
                    .getOwned()
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .map<CreateQuizChange> { CreateQuizChange.FinishLoad(it) }
                    .onErrorReturn { CreateQuizChange.Error(it) }
                    .startWith(CreateQuizChange.Loading)
        }


        val loadAction = actions.ofType<CreateQuizAction.Load>()
                .switchMap { loadQuizzes() }

        val refreshAction = actions.ofType<CreateQuizAction.Refresh>()
                .switchMap { loadQuizzes() }

        val expandAction = actions.ofType<CreateQuizAction.ToggleExpansion>()
                .map { CreateQuizChange.ToggleExpansion(it.quiz) }


        val deleteAction = actions.ofType<CreateQuizAction.DeleteQuiz>()
                .flatMap {
                    return@flatMap quizRepository
                            .deleteQuiz(it.quiz)
                            .observeOn(AndroidSchedulers.mainThread())
                            .toObservable<Unit>()
                            .onErrorReturn { err -> CreateQuizChange.Error(err) }
                            .map { loadQuizzes.invoke().blockingFirst() }
                            .startWith { CreateQuizChange.Loading }
                }


        val actions = Observable.merge(loadAction, refreshAction, deleteAction, expandAction)

        disposables += actions
                .scan(initialState, reducer)
                .distinctUntilChanged()
                .subscribe(state::postValue)
    }
}

private fun <E> Set<E>.toggle(quiz: E): Set<E> {
    return if (this.contains(quiz)) {
        this - quiz
    } else {
        this + quiz
    }
}
