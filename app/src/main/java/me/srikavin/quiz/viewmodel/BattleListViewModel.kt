package me.srikavin.quiz.viewmodel

import com.ww.roxie.BaseAction
import com.ww.roxie.BaseState
import com.ww.roxie.BaseViewModel
import com.ww.roxie.Reducer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.QuizRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.get

sealed class BattleListAction : BaseAction {
    object Load : BattleListAction()
    object Refresh : BattleListAction()
}

sealed class BattleListChange {
    object Loading : BattleListChange()
    data class FinishLoad(val quizzes: List<Quiz>) : BattleListChange()
    data class Error(val error: Throwable?) : BattleListChange()
}


data class BattleListState(
        val quizzes: List<Quiz> = emptyList(),
        val loading: Boolean = false,
        val loaded: Boolean = false,
        val error: Boolean = false
) : BaseState

class BattleListViewModel(initialState: BattleListState?) : BaseViewModel<BattleListAction, BattleListState>(), KoinComponent {
    private val quizRepository: QuizRepository = get()
    override val initialState = initialState ?: BattleListState()

    private val reducer: Reducer<BattleListState, BattleListChange> = { state, change ->
        when (change) {
            is BattleListChange.Loading -> state.copy(
                    loading = true,
                    error = false
            )

            is BattleListChange.FinishLoad -> state.copy(
                    quizzes = change.quizzes,
                    loading = false,
                    loaded = true,
                    error = false
            )

            is BattleListChange.Error -> state.copy(
                    error = true,
                    loading = false
            )
        }
    }

    init {
        bindActions()
    }

    private fun bindActions() {
        val loadQuizzes = {
            quizRepository
                    .getQuizzes()
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .map<BattleListChange> { BattleListChange.FinishLoad(it) }
                    .onErrorReturn { BattleListChange.Error(it) }
                    .startWith(BattleListChange.Loading)
        }


        val loadAction = actions.ofType<BattleListAction.Load>()
                .switchMap { loadQuizzes() }

        val refreshAction = actions.ofType<BattleListAction.Refresh>()
                .switchMap { loadQuizzes() }

        val actions = Observable.merge(loadAction, refreshAction)

        disposables += actions
                .scan(initialState, reducer)
                .distinctUntilChanged()
                .subscribe(state::postValue)
    }
}