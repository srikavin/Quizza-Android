package me.srikavin.quiz.viewmodel

import android.content.Context
import com.ww.roxie.*
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import me.srikavin.quiz.model.AuthUser
import me.srikavin.quiz.repository.AuthRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.get

/**
 * Possible actions that can be relayed to this view model.
 */
sealed class ProfileAction : BaseAction {
    /**
     * Requests loading the current user profile
     */
    object Load : ProfileAction()

    /**
     * Requests to logout of the current user
     */
    object Logout : ProfileAction()
}

/**
 * State changes that are derived from [ProfileViewState] and applied to [ProfileViewState]
 */
sealed class ProfileChange {
    internal data class FinishLoad(val user: AuthUser?) : ProfileChange()
    internal data class Error(val error: Throwable?) : ProfileChange()
}

/**
 * The state of the view. This should be used to render the UI.
 */
data class ProfileViewState(
        /**
         * Whether data is still being loaded
         */
        val loading: Boolean = false,
        /**
         * Whether an error has occurred
         */
        val error: Boolean = false,
        /**
         * The user currently logged in, null indicates offline
         */
        val user: AuthUser? = null
) : BaseState

/**
 * Manages the state behind [CreateFragment][me.srikavin.quiz.view.main.CreateFragment] and handles
 * any actions triggered, resulting in changes to the state.
 */
class ProfileViewModel(initialState: ProfileViewState?) : BaseViewModel<ProfileAction, ProfileViewState>(), KoinComponent {
    /**
     * The state to initialize the view model.
     */
    override val initialState = initialState ?: ProfileViewState()
    private val context: Context = get()

    private val reducer: Reducer<ProfileViewState, ProfileChange> = { state, change ->
        when (change) {
            is ProfileChange.FinishLoad -> state.copy(
                    user = change.user,
                    loading = false,
                    error = false
            )

            is ProfileChange.Error -> state.copy(
                    error = true,
                    loading = false
            )
        }
    }


    init {
        bindActions()
        Roxie.enableLogging()
    }

    private fun bindActions() {
        val loadAction = actions.ofType<ProfileAction.Load>()
                .map<ProfileChange> {
                    return@map ProfileChange.FinishLoad(AuthRepository.getUser())
                }

        val logoutAction = actions.ofType<ProfileAction.Logout>()
                .map<ProfileChange> {
                    AuthRepository.logout(context)
                    return@map ProfileChange.FinishLoad(null)
                }

        disposables += Observable.merge(loadAction, logoutAction)
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
