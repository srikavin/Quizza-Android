package me.srikavin.quiz.view.detail

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.squareup.picasso.Picasso
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.view.game.GameActivity
import me.srikavin.quiz.viewmodel.QuizDetailAction
import me.srikavin.quiz.viewmodel.QuizDetailState
import me.srikavin.quiz.viewmodel.QuizDetailViewModel

const val ARG_QUIZ_ID = "id"
const val ARG_QUIZ_NAME = "quiz_name"

class QuizDetailFragment : Fragment() {

    private lateinit var viewModel: QuizDetailViewModel
    private lateinit var titleToolbar: CollapsingToolbarLayout
    private lateinit var coverImage: ImageView
    private lateinit var description: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(
                this,
                QuizDetailViewModelFactory(this)
        ).get(QuizDetailViewModel::class.java)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        titleToolbar = view.findViewById(R.id.collapsing_toolbar)
        description = view.findViewById(R.id.quiz_detail_description)

        Picasso.get().load(null as String?)
                .placeholder(ColorDrawable(resources.getColor(R.color.colorSecondaryLight, null)))
                .into(view.findViewById<View>(R.id.image) as ImageView)

        val id = arguments!!.getString(ARG_QUIZ_ID)!!
        val quizName = arguments?.getString(ARG_QUIZ_NAME)

        if (quizName != null) {
            titleToolbar.title = quizName
        }

        viewModel.dispatch(QuizDetailAction.LoadQuiz(id))
        viewModel.observableState.observe(this, Observer(this::render))

        view.findViewById<View>(R.id.quiz_detail_battle_fab).setOnClickListener { this.handleBattleClick(id) }
    }

    private fun handleBattleClick(id: String) {
        val activity = requireActivity()

        val intent = Intent(activity, GameActivity::class.java)
        intent.putExtra(GameActivity.EXTRA_QUIZ_ID, id)
        activity.startActivity(intent)
    }

    private fun render(state: QuizDetailState) {
        println(state)
        with(state) {
            when {
                error -> renderErrorState()
                loading -> renderLoadingState()
                else -> renderQuiz(quiz)
            }
        }
    }

    private fun renderLoadingState() {

    }

    private fun renderErrorState() {
        Toast.makeText(context, R.string.data_load_fail, Toast.LENGTH_LONG).show()
        activity?.finish()
    }

    private fun renderQuiz(quiz: Quiz?) {
        if (quiz == null) {
            renderErrorState()
            return
        }

        titleToolbar.title = quiz.title


        if (quiz.coverImage != null) {
            Picasso.get().load(quiz.coverImage).into(coverImage)
        }

        description.text = quiz.description ?: ""

    }

    companion object {
        fun newInstance(id: String, quizName: String? = null): QuizDetailFragment {
            val fragment = QuizDetailFragment()
            val args = Bundle().apply {
                putString(ARG_QUIZ_ID, id)
                putString(ARG_QUIZ_NAME, quizName)
            }
            fragment.arguments = args
            return fragment
        }
    }

}
