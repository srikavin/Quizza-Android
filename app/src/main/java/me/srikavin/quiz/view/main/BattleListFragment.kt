package me.srikavin.quiz.view.main

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.Fade
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.view.detail.DetailsTransition
import me.srikavin.quiz.view.detail.QuizDetailFragment
import me.srikavin.quiz.viewmodel.BattleListAction
import me.srikavin.quiz.viewmodel.BattleListState
import me.srikavin.quiz.viewmodel.BattleListViewModel


class BattleListFragment : Fragment() {
    private lateinit var viewModel: BattleListViewModel
    private lateinit var adapter: BattleQuizListAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle_quiz_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        viewModel = ViewModelProviders.of(this, BattleListModelFactory()).get(BattleListViewModel::class.java)
        viewModel.observableState.observe(this, Observer<BattleListState>(this::render))

        val recyclerView = view.findViewById<RecyclerView>(R.id.battle_quizzes_recycler_view)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        swipeRefresh = view.findViewById(R.id.battle_swipe_refresh)
        swipeRefresh.setOnRefreshListener { this.updateQuizzes() }

        adapter = BattleQuizListAdapter(this::onQuizClick, ArrayList())

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                startPostponedEnterTransition()
                return true
            }
        })

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val halfSpace = 8

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.paddingLeft != halfSpace) {
                    parent.setPadding(halfSpace, halfSpace, halfSpace, halfSpace)
                    parent.clipToPadding = true
                }

                outRect.set(halfSpace, halfSpace, halfSpace, halfSpace)
            }
        })

        if (viewModel.observableState.value?.loaded != true) {
            viewModel.dispatch(BattleListAction.Load)
        }

    }

    private fun onQuizClick(quiz: Quiz, vh: QuizListViewHolder, position: Int) {
        val fragment = QuizDetailFragment.newInstance(
                quiz.id.idString,
                quiz.title,
                "quiz_title_$position",
                "quiz_image_$position"
        )
        fragment.sharedElementReturnTransition = DetailsTransition()
        fragment.sharedElementEnterTransition = DetailsTransition()
        fragment.enterTransition = Fade()
        fragment.exitTransition = Fade()

        (fragmentManager ?: return).beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.battle_fragment_container, fragment)
                .addSharedElement(vh.image, "quiz_image_$position")
                .addSharedElement(vh.title, "quiz_title_$position")
                .addToBackStack(null)
                .commit()
    }

    private fun render(state: BattleListState) {
        with(state) {
            when {
                loading -> renderLoading()
                error -> renderError()
                else -> renderQuizzes(quizzes)
            }
        }
    }

    private fun renderQuizzes(quizzes: List<Quiz>) {
        swipeRefresh.isRefreshing = false
        adapter.quizzes = quizzes
    }

    private fun renderError() {
        swipeRefresh.isRefreshing = false
        Toast.makeText(context, R.string.error_network, Toast.LENGTH_SHORT).show()
    }

    private fun renderLoading() {
        swipeRefresh.isRefreshing = true
    }

    private fun updateQuizzes() {
        viewModel.dispatch(BattleListAction.Refresh)
    }


    companion object {
        fun newInstance(): BattleListFragment {
            return BattleListFragment()
        }
    }

}
