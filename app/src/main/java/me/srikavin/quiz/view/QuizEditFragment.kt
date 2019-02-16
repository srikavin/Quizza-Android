package me.srikavin.quiz.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.model.QuizAnswer
import me.srikavin.quiz.model.QuizQuestion
import me.srikavin.quiz.viewmodel.QuizEditViewModel
import java.util.*

class QuizEditFragment : Fragment() {

    private lateinit var quizLiveData: LiveData<Quiz>
    private var quiz: Quiz? = null
    private var mViewModel: QuizEditViewModel? = null
    private var questionAdapter: QuestionAdapter? = null
    private var noQuestionOverlay: View? = null
    private var quizTitle: EditText? = null
    private var questions: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_edit_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(QuizEditViewModel::class.java)

        questions = view.findViewById(R.id.quiz_edit_questions_list)
        val toolbar = view.findViewById<Toolbar>(R.id.create_toolbar)
        val createQuestionFab = view.findViewById<FloatingActionButton>(R.id.create_question_fab)
        val createQuestionButton = view.findViewById<AppCompatButton>(R.id.create_question_button)
        quizTitle = view.findViewById(R.id.quiz_edit_quiz_title)
        noQuestionOverlay = view.findViewById(R.id.quiz_edit_no_questions_state)

        val mode = arguments!!.get("mode") as Mode
        val id = arguments!!.getString("id")

        quizLiveData = when (mode) {
            QuizEditFragment.Mode.CREATE -> mViewModel!!.createQuiz()
            QuizEditFragment.Mode.EDIT -> mViewModel!!.editQuiz(id!!)
        }

        quizLiveData.observe(this, Observer { quiz ->
            this@QuizEditFragment.quiz = quiz
            update(quiz)
        })


        toolbar.inflateMenu(R.menu.quiz_edit_toolbar_menu)


        class MenuItemListener : MenuItem.OnMenuItemClickListener {
            private fun save() {
                mViewModel!!.saveQuiz().observe(viewLifecycleOwner, Observer { quiz ->
                    if (quiz != null) {
                        if (!quiz.isLocal) {
                            Toast.makeText(context, getString(R.string.data_save_success), Toast.LENGTH_SHORT).show()
                        }
                        activity!!.finish()
                    } else {
                        Toast.makeText(context, getString(R.string.data_save_fail), Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.quiz_edit_toolbar_publish) {
                    // Verify user wants to publish publicly
                    val builder = AlertDialog.Builder(context!!)
                    builder.setTitle(getString(R.string.edit_quiz_publish_title))
                    builder.setMessage(getString(R.string.edit_quiz_publish_warning))
                    builder.setPositiveButton(getString(R.string.edit_quiz_publish_confirm_text)) { dialog, _ ->
                        quiz!!.draft = false
                        save()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton(getString(R.string.edit_quiz_publish_cancel_text)) { dialog, _ -> dialog.dismiss() }

                    val alert = builder.create()
                    alert.show()
                } else {
                    save()
                }
                return true
            }
        }

        val menuItemListener = MenuItemListener()

        toolbar.menu.findItem(R.id.quiz_edit_toolbar_save).setOnMenuItemClickListener(menuItemListener)
        toolbar.menu.findItem(R.id.quiz_edit_toolbar_publish).setOnMenuItemClickListener(menuItemListener)

        createQuestionFab.setOnClickListener { createQuestion() }

        createQuestionButton.setOnClickListener { createQuestion() }

        toolbar.title = "Create Quiz"

        quizTitle!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                quiz!!.title = s.toString()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

    }

    private fun update(quiz: Quiz) {
        quizTitle!!.setText(quiz.title)
        questionAdapter = QuestionAdapter()
        questions!!.adapter = questionAdapter
        questions!!.layoutManager = LinearLayoutManager(context)
        updateNoQuestionOverlay()
    }

    private fun updateNoQuestionOverlay() {
        noQuestionOverlay!!.visibility = if (quiz!!.questions.size == 0) View.VISIBLE else View.GONE
    }

    private fun createQuestion() {
        val newQuestion = QuizQuestion()
        newQuestion.answers.add(QuizAnswer())
        quiz!!.questions.add(newQuestion)
        questionAdapter!!.notifyDataSetChanged()
        updateNoQuestionOverlay()
    }

    private fun deleteQuestion(question: QuizQuestion) {
        quiz!!.questions.remove(question)
        questionAdapter!!.notifyDataSetChanged()
        updateNoQuestionOverlay()
    }

    enum class Mode {
        CREATE,
        EDIT
    }

    private class AnswerAdapter(private val question: QuizQuestion, private val questionViewHolder: QuestionViewHolder) : RecyclerView.Adapter<AnswerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
            return AnswerViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.quiz_edit_answer_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
            holder.bind(question.answers[position], this)
        }

        override fun getItemCount(): Int {
            return question.answers.size
        }

        fun removeAnswer(answer: QuizAnswer) {
            questionViewHolder.removeAnswer(answer)
            notifyDataSetChanged()
        }
    }

    private class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var listener: TextListener? = null

        fun bind(answer: QuizAnswer, answerAdapter: AnswerAdapter) {
            val checkBox = itemView.findViewById<CheckBox>(R.id.answer_list_item_correct)
            val textView = itemView.findViewById<TextView>(R.id.answer_list_item_answer_text)

            if (listener == null) {
                listener = TextListener(answer)
                textView.addTextChangedListener(listener)
            }

            listener!!.setAnswer(answer)

            itemView.findViewById<View>(R.id.answer_list_item_delete).setOnClickListener { answerAdapter.removeAnswer(answer) }

            checkBox.setOnCheckedChangeListener { _, isChecked -> answer.isCorrect = isChecked }

            checkBox.isChecked = answer.isCorrect
            textView.text = answer.contents
        }

        private class TextListener(private var answer: QuizAnswer?) : TextWatcher {

            fun setAnswer(answer: QuizAnswer) {
                this.answer = answer
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                answer!!.contents = s.toString()

            }

            override fun afterTextChanged(s: Editable) {

            }
        }
    }

    private inner class QuestionAdapter : RecyclerView.Adapter<QuestionViewHolder>() {
        internal var expanded: MutableMap<QuizQuestion, Boolean> = HashMap()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
            return QuestionViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.quiz_edit_create_question, parent, false))

        }

        override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
            val question = quiz!!.questions[position]
            var isExpanded = expanded[question]
            if (isExpanded == null) {
                isExpanded = false
            }

            holder.setQuestion(question, isExpanded)
            holder.itemView.setOnClickListener {
                var cur = expanded[question]
                if (cur == null) {
                    cur = false
                }
                expanded[question] = !cur
                notifyItemChanged(position)
            }
        }

        override fun getItemCount(): Int {
            return quiz!!.questions.size
        }
    }

    private inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var question: QuizQuestion? = null
        private var questionDetails: TextView? = null
        private var listener: TextListener? = null
        private lateinit var questionTitle: TextView

        fun setQuestion(question: QuizQuestion, expanded: Boolean) {
            this.question = question

            println(question)

            val answersList = itemView.findViewById<RecyclerView>(R.id.create_question_answer_list)
            questionTitle = itemView.findViewById(R.id.create_question_title)
            val questionText = itemView.findViewById<EditText>(R.id.create_question_question_text)
            questionDetails = itemView.findViewById(R.id.create_question_subtitle)

            val adapter = AnswerAdapter(question, this)

            if (listener == null) {
                listener = TextListener(question)
                questionText.addTextChangedListener(listener)
            }
            listener!!.setQuestion(question)

            answersList.adapter = adapter
            answersList.layoutManager = LinearLayoutManager(context)

            if (expanded) {
                itemView.findViewById<View>(R.id.create_questions_question_details).visibility = View.VISIBLE
            } else {
                itemView.findViewById<View>(R.id.create_questions_question_details).visibility = View.GONE
            }

            itemView.findViewById<View>(R.id.create_question_add_answer).setOnClickListener {
                createAnswer()
                adapter.notifyDataSetChanged()
            }

            itemView.findViewById<View>(R.id.create_question_delete_question).setOnClickListener {
                val builder = AlertDialog.Builder(context!!)
                builder.setTitle(getString(R.string.delete_question_confirm_title))
                builder.setMessage(getString(R.string.delete_question_confirm))
                builder.setPositiveButton(getString(R.string.delete_question_delete_button)) { dialog, _ ->
                    deleteQuestion(question)
                    dialog.dismiss()
                }
                builder.setNegativeButton(getString(R.string.delete_question_cancel_button)) { dialog, _ -> dialog.dismiss() }

                val alert = builder.create()
                alert.show()
            }


            questionTitle.text = question.contents
            questionText.setText(question.contents)

            updateAnswerDetails()
        }

        private fun updateAnswerDetails() {
            val count = question!!.answers.size
            questionDetails!!.text = resources.getQuantityString(R.plurals.contains_x_answers, count, count)
        }

        private fun createAnswer() {
            question!!.answers.add(QuizAnswer())
            println(Gson().toJson(quiz))
            updateAnswerDetails()
        }

        fun removeAnswer(answer: QuizAnswer) {
            question!!.answers.remove(answer)
            println(Gson().toJson(quiz))
            updateAnswerDetails()
        }

        private inner class TextListener(private var question: QuizQuestion?) : TextWatcher {

            fun setQuestion(question: QuizQuestion) {
                this.question = question
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val title = s.toString()
                question!!.contents = title
                questionTitle.text = title
            }

            override fun afterTextChanged(s: Editable) {

            }
        }

    }

    companion object {


        fun newInstance(mode: Mode, quizId: String?): QuizEditFragment {
            val bundle = Bundle()
            bundle.putSerializable("mode", mode)
            bundle.putSerializable("id", quizId)

            val fragment = QuizEditFragment()
            fragment.retainInstance = true
            fragment.arguments = bundle

            return fragment
        }
    }

}
