package me.srikavin.quiz.view.main

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.view.detail.ARG_QUIZ_ID
import me.srikavin.quiz.view.detail.ARG_QUIZ_NAME
import me.srikavin.quiz.view.detail.QuizDetail
import me.srikavin.quiz.viewmodel.BattleListAction
import me.srikavin.quiz.viewmodel.BattleListState
import me.srikavin.quiz.viewmodel.BattleViewModel

class BattleFragment : Fragment() {
    private lateinit var viewModel: BattleViewModel
    private lateinit var adapter: BattleQuizListAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, BattleListModelFactory(this)).get(BattleViewModel::class.java)
        viewModel.observableState.observe(this, Observer<BattleListState>(this::render))

        val recyclerView = view.findViewById<RecyclerView>(R.id.battle_quizzes_recycler_view)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        swipeRefresh = view.findViewById(R.id.battle_swipe_refresh)
        swipeRefresh.setOnRefreshListener { this.updateQuizzes() }

        adapter = BattleQuizListAdapter(this::onQuizClick, ArrayList())

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

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

        viewModel.dispatch(BattleListAction.Load)
    }

    private fun onQuizClick(quiz: Quiz) {
        val intent = Intent(context, QuizDetail::class.java)
        intent.putExtra(ARG_QUIZ_ID, quiz.id)
        intent.putExtra(ARG_QUIZ_NAME, quiz.title)
        context?.startActivity(intent)
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
        //TODO: Create a loading state
    }

    private fun updateQuizzes() {
        viewModel.dispatch(BattleListAction.Refresh)
    }

}
