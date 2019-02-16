package me.srikavin.quiz.view.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.view.QuizEditActivity
import me.srikavin.quiz.view.QuizEditFragment
import me.srikavin.quiz.viewmodel.CreateQuizAction
import me.srikavin.quiz.viewmodel.CreateQuizState
import me.srikavin.quiz.viewmodel.CreateViewModel

class CreateFragment : Fragment() {

    private lateinit var viewModel: CreateViewModel
    private lateinit var noQuizOverlay: View
    private lateinit var adapter: QuizAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, CreateViewModelFactory(this)).get(CreateViewModel::class.java)

        noQuizOverlay = view.findViewById(R.id.create_no_quiz_overlay)

        view.findViewById<View>(R.id.create_question_button).setOnClickListener { createNewQuiz() }
        view.findViewById<View>(R.id.create_fab).setOnClickListener { createNewQuiz() }

        adapter = QuizAdapter()
        adapter.onToggleExpand = { quiz ->
            viewModel.dispatch(CreateQuizAction.ToggleExpansion(quiz))
        }

        val draftList = view.findViewById<RecyclerView>(R.id.create_draft_recycler)
        draftList.layoutManager = LinearLayoutManager(this.context)
        draftList.adapter = adapter

        viewModel.observableState.observe(this, Observer<CreateQuizState> {
            render(it)
            println(it.expanded)
        })

        viewModel.dispatch(CreateQuizAction.Load)

    }

    private fun render(state: CreateQuizState) {
        with(state) {
            when {
                loading -> renderLoading()
                error -> renderError()
                else -> renderQuizzes(state.quizzes, state.expanded)
            }
        }
    }

    private fun renderLoading() {

    }

    private fun renderError() {

    }

    private fun renderQuizzes(quizzes: List<Quiz>, expanded: Set<Quiz>) {
        updateNoQuizOverlay(quizzes)
        adapter.quizzes = quizzes
        adapter.expanded = expanded
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    private fun update() {
        viewModel.dispatch(CreateQuizAction.Refresh)
    }

    private fun deleteQuiz(quiz: Quiz) {
        viewModel.dispatch(CreateQuizAction.DeleteQuiz(quiz))
    }

    private fun editQuiz(quiz: Quiz) {
        val activity = requireActivity()

        val intent = Intent(activity, QuizEditActivity::class.java)
        intent.putExtra("mode", QuizEditFragment.Mode.EDIT)
        intent.putExtra("id", quiz.id.idString)

        activity.startActivityForResult(intent, RECV_EDIT_QUIZ)

    }

    private fun createNewQuiz() {
        val activity = requireActivity()

        val intent = Intent(activity, QuizEditActivity::class.java)
        intent.putExtra("mode", QuizEditFragment.Mode.CREATE)
        activity.startActivityForResult(intent, RECV_CREATE_QUIZ)
    }

    private fun updateNoQuizOverlay(quizzes: List<Quiz>) {
        noQuizOverlay.visibility = if (quizzes.isEmpty()) View.VISIBLE else View.GONE
    }

    private inner class QuizAdapter : RecyclerView.Adapter<QuizViewHolder>() {
        internal var expanded: Set<Quiz> = emptySet()
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        internal var quizzes: List<Quiz> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        var onToggleExpand: (Quiz) -> Unit = {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
            return QuizViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.create_quiz_list_item, parent, false))

        }

        override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
            val quiz = quizzes[position]
            holder.setQuiz(quiz, expanded.contains(quiz))
            holder.itemView.setOnClickListener {
                onToggleExpand(quiz)
            }
        }

        override fun getItemCount(): Int {
            return quizzes.size
        }
    }

    private inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setQuiz(quiz: Quiz, expanded: Boolean) {
            val quizTitle = itemView.findViewById<TextView>(R.id.create_quiz_title)
            val quizSubtitle = itemView.findViewById<TextView>(R.id.create_quiz_subtitle)
            val quizStatus = itemView.findViewById<TextView>(R.id.create_quiz_status)

            quizTitle.text = quiz.title
            if (quiz.draft) {
                quizStatus.text = getString(R.string.create_quiz_draft)
                quizStatus.setTextColor(resources.getColor(R.color.colorSecondaryDark))
            } else {
                quizStatus.text = getString(R.string.create_quiz_published)
                quizStatus.setTextColor(resources.getColor(R.color.colorPrimary))
            }
            val questions = quiz.questions.size
            quizSubtitle.text = resources.getQuantityString(R.plurals.contains_x_questions, questions, questions)

            if (expanded) {
                itemView.findViewById<View>(R.id.create_quiz_details).visibility = View.VISIBLE
            } else {
                itemView.findViewById<View>(R.id.create_quiz_details).visibility = View.GONE
            }

            itemView.findViewById<View>(R.id.create_quiz_delete_quiz).setOnClickListener { v ->
                val builder = AlertDialog.Builder(context!!)
                builder.setTitle(getString(R.string.delete_quiz_title))
                builder.setMessage(getString(R.string.delete_quiz_confirmm))
                builder.setPositiveButton("Delete Quiz") { dialog, _ ->
                    deleteQuiz(quiz)
                    dialog.dismiss()
                }
                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

                val alert = builder.create()
                alert.show()
            }
            itemView.findViewById<View>(R.id.create_quiz_edit_quiz).setOnClickListener { editQuiz(quiz) }
        }
    }

    companion object {
        const val RECV_CREATE_QUIZ = 1
        const val RECV_EDIT_QUIZ = 2
    }
}
