package me.srikavin.quiz.view.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.view.game.GameActivity
import me.srikavin.quiz.viewmodel.QuizDetailAction
import me.srikavin.quiz.viewmodel.QuizDetailState
import me.srikavin.quiz.viewmodel.QuizDetailViewModel

private const val ARG_QUIZ_ID = "id"
private const val ARG_QUIZ_NAME = "quiz_name"
private const val ARG_IMAGE_TRANSITION_NAME = "image_transition_name"
private const val ARG_TITLE_TRANSITION_NAME = "title_transition_name"

class QuizDetailFragment : Fragment() {

    private lateinit var viewModel: QuizDetailViewModel
    private lateinit var coverImage: ImageView
    private lateinit var description: TextView
    private lateinit var title: TextView
    private lateinit var author: TextView
    private lateinit var questionRecycler: RecyclerView
    private lateinit var questionRecyclerAdapter: QuestionRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = view.findViewById(R.id.detail_fragment_quiz_title)
        coverImage = view.findViewById(R.id.detail_fragment_quiz_image)
        description = view.findViewById(R.id.detail_fragment_quiz_description)
        author = view.findViewById(R.id.detail_fragment_quiz_author)
        questionRecycler = view.findViewById(R.id.detail_fragment_quiz_questions_recycler)

        val args = arguments!!

        val id = args.getString(ARG_QUIZ_ID)!!

        args.getString(ARG_IMAGE_TRANSITION_NAME)?.let {
            coverImage.transitionName = it
        }

        args.getString(ARG_TITLE_TRANSITION_NAME)?.let {
            title.transitionName = it
        }

        args.getString(ARG_QUIZ_NAME)?.let {
            title.text = it
        }

        questionRecyclerAdapter = QuestionRecyclerAdapter(emptyList(), requireContext())
        questionRecycler.adapter = questionRecyclerAdapter
        questionRecycler.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        viewModel = ViewModelProviders.of(
                this,
                QuizDetailViewModelFactory(this)
        ).get(QuizDetailViewModel::class.java)


        Picasso.get().load(null as String?)
                .placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.ic_app_logo)!!)
                .into(coverImage)


        viewModel.dispatch(QuizDetailAction.LoadQuiz(id))
        viewModel.observableState.observe(this, Observer(this::render))

        view.findViewById<View>(R.id.game_mode_select_online).setOnClickListener {
            handleBattleClick(id)
        }
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

        title.text = quiz.title


        if (quiz.coverImage != null) {
            Picasso.get().load(quiz.coverImage).into(coverImage)
        }

        description.text = quiz.description

        questionRecyclerAdapter.questions = quiz.questions

        if (quiz.author != null) {
            author.text = quiz.author.username
        }
    }

    companion object {
        fun newInstance(
                id: String,
                quizName: String? = null,
                titleTransitionName: String = "quiz_title",
                imageTransitionName: String = "image_title"
        ): QuizDetailFragment {
            val fragment = QuizDetailFragment()
            val args = Bundle().apply {
                putString(ARG_QUIZ_ID, id)
                putString(ARG_QUIZ_NAME, quizName)
                putString(ARG_TITLE_TRANSITION_NAME, titleTransitionName)
                putString(ARG_IMAGE_TRANSITION_NAME, imageTransitionName)
            }
            fragment.arguments = args
            return fragment
        }
    }

}
