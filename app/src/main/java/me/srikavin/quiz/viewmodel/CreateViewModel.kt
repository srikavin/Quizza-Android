package me.srikavin.quiz.viewmodel

import com.ww.roxie.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.repository.QuizRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.get

/**
 * Possible actions that can be relayed to this view model.
 */
sealed class CreateQuizAction : BaseAction {
    /**
     * Requests loading the quizzes
     */
    object Load : CreateQuizAction()

    /**
     * Requests refreshing the current quizzes
     */
    object Refresh : CreateQuizAction()

    /**
     * Requests the deletion of a quiz
     */
    data class DeleteQuiz(
            /**
             * The quiz to delete
             */
            val quiz: Quiz
    ) : CreateQuizAction()

    /**
     * Requests the expansion of a quiz
     */
    data class ToggleExpansion(
            /**
             * The quiz to toggle expansion for
             */
            val quiz: Quiz
    ) : CreateQuizAction()
}

/**
 * Quiz state changes that are derived from [CreateQuizAction] and applied to [CreateQuizState]
 */
sealed class CreateQuizChange {
    internal object Loading : CreateQuizChange()
    internal data class FinishLoad(val quizzes: List<Quiz>) : CreateQuizChange()
    internal data class Error(val error: Throwable?) : CreateQuizChange()
    internal data class ToggleExpansion(val quiz: Quiz) : CreateQuizChange()
}

/**
 * The state of the view. This should be used to render the UI.
 */
data class CreateQuizState(
        /**
         * A list of quizzes loaded
         */
        val quizzes: List<Quiz> = emptyList(),
        /**
         * Whether network requests are still in progress
         */
        val loading: Boolean = false,
        /**
         * Whether an error occurred
         */
        val error: Boolean = false,
        /**
         * A set of expanded quizzes
         */
        val expanded: Set<Quiz> = HashSet()
) : BaseState

/**
 * Manages the state behind [CreateFragment][me.srikavin.quiz.view.main.CreateFragment] and handles
 * any actions triggered, resulting in changes to the state.
 */
class CreateViewModel(initialState: CreateQuizState?) : BaseViewModel<CreateQuizAction, CreateQuizState>(), KoinComponent {
    /**
     * The state to initialize the view model.
     */
    override val initialState = initialState ?: CreateQuizState()
    private val quizRepository: QuizRepository = get()

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
                .switchMap {
                    return@switchMap quizRepository
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
