package me.srikavin.quiz.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.model.QuizAnswer;
import me.srikavin.quiz.model.QuizQuestion;
import me.srikavin.quiz.viewmodel.QuizEditViewModel;

public class QuizEditFragment extends Fragment {

    private LiveData<Quiz> quizLiveData;
    private Quiz quiz;
    private QuizEditViewModel mViewModel;
    private QuestionAdapter questionAdapter;
    private View noQuestionOverlay;
    private EditText quizTitle;
    private RecyclerView questions;


    public static QuizEditFragment newInstance(@NonNull Mode mode, @Nullable String quizId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("mode", mode);
        bundle.putSerializable("id", quizId);

        QuizEditFragment fragment = new QuizEditFragment();
        fragment.setRetainInstance(true);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quiz_edit_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(QuizEditViewModel.class);

        questions = getView().findViewById(R.id.quiz_edit_questions_list);
        Toolbar toolbar = getView().findViewById(R.id.create_toolbar);
        FloatingActionButton createQuestionFab = getView().findViewById(R.id.create_question_fab);
        AppCompatButton createQuestionButton = getView().findViewById(R.id.create_question_button);
        quizTitle = getView().findViewById(R.id.quiz_edit_quiz_title);
        noQuestionOverlay = getView().findViewById(R.id.quiz_edit_no_questions_state);

        assert getArguments() != null;

        Mode mode = (Mode) getArguments().get("mode");
        String id = getArguments().getString("id");

        assert mode != null;

        switch (mode) {
            case CREATE:
                quizLiveData = mViewModel.createQuiz();
                break;
            case EDIT:
                quizLiveData = mViewModel.editQuiz(id);
                break;
        }

        quizLiveData.observe(this, quiz -> {
            QuizEditFragment.this.quiz = quiz;
            update(quiz);
        });


        toolbar.inflateMenu(R.menu.quiz_edit_toolbar_menu);


        class MenuItemListener implements MenuItem.OnMenuItemClickListener {
            private void save() {
                mViewModel.saveQuiz().observe(getViewLifecycleOwner(), quiz -> {
                    if (quiz != null) {
                        if (!quiz.isLocal) {
                            Toast.makeText(getContext(), getString(R.string.data_save_success), Toast.LENGTH_SHORT).show();
                        }
                        getActivity().finish();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.data_save_fail), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.quiz_edit_toolbar_publish) {
                    // Verify user wants to publish publicly
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getString(R.string.edit_quiz_publish_title));
                    builder.setMessage(getString(R.string.edit_quiz_publish_warning));
                    builder.setPositiveButton(getString(R.string.edit_quiz_publish_confirm_text), (dialog, id12) -> {
                        quiz.draft = false;
                        save();
                        dialog.dismiss();
                    });
                    builder.setNegativeButton(getString(R.string.edit_quiz_publish_cancel_text), (dialog, id1) -> dialog.dismiss());

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    save();
                }
                return true;
            }
        }

        MenuItemListener menuItemListener = new MenuItemListener();

        toolbar.getMenu().findItem(R.id.quiz_edit_toolbar_save).setOnMenuItemClickListener(menuItemListener);
        toolbar.getMenu().findItem(R.id.quiz_edit_toolbar_publish).setOnMenuItemClickListener(menuItemListener);

        createQuestionFab.setOnClickListener(v -> createQuestion());

        createQuestionButton.setOnClickListener(v -> createQuestion());

        toolbar.setTitle("Create Quiz");

        quizTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                quiz.title = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void update(Quiz quiz) {
        quizTitle.setText(quiz.title);
        questionAdapter = new QuestionAdapter();
        questions.setAdapter(questionAdapter);
        questions.setLayoutManager(new LinearLayoutManager(getContext()));
        updateNoQuestionOverlay();
    }

    private void updateNoQuestionOverlay() {
        noQuestionOverlay.setVisibility(quiz.questions.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private void createQuestion() {
        QuizQuestion newQuestion = new QuizQuestion();
        newQuestion.answers.add(new QuizAnswer());
        quiz.questions.add(newQuestion);
        questionAdapter.notifyDataSetChanged();
        updateNoQuestionOverlay();
    }

    private void deleteQuestion(QuizQuestion question) {
        quiz.questions.remove(question);
        questionAdapter.notifyDataSetChanged();
        updateNoQuestionOverlay();
    }

    public enum Mode {
        CREATE,
        EDIT
    }

    private static class AnswerAdapter extends RecyclerView.Adapter<AnswerViewHolder> {
        private QuizQuestion question;
        private QuestionViewHolder questionViewHolder;

        public AnswerAdapter(QuizQuestion question, QuestionViewHolder questionViewHolder) {
            this.question = question;
            this.questionViewHolder = questionViewHolder;
        }

        @NonNull
        @Override
        public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AnswerViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.quiz_edit_answer_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AnswerViewHolder holder, final int position) {
            holder.bind(question.answers.get(position), this);
        }

        @Override
        public int getItemCount() {
            return question.answers.size();
        }

        public void removeAnswer(QuizAnswer answer) {
            questionViewHolder.removeAnswer(answer);
            notifyDataSetChanged();
        }
    }

    private static class AnswerViewHolder extends RecyclerView.ViewHolder {
        private TextListener listener;

        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(final QuizAnswer answer, final AnswerAdapter answerAdapter) {
            CheckBox checkBox = itemView.findViewById(R.id.answer_list_item_correct);
            TextView textView = itemView.findViewById(R.id.answer_list_item_answer_text);

            if (listener == null) {
                listener = new TextListener(answer);
                textView.addTextChangedListener(listener);
            }

            listener.setAnswer(answer);

            itemView.findViewById(R.id.answer_list_item_delete).setOnClickListener(v -> answerAdapter.removeAnswer(answer));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> answer.correct = isChecked);

            checkBox.setChecked(answer.correct);
            textView.setText(answer.text);
        }

        private static class TextListener implements TextWatcher {

            private QuizAnswer answer;

            public TextListener(QuizAnswer answer) {
                this.answer = answer;
            }

            public void setAnswer(QuizAnswer answer) {
                this.answer = answer;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                answer.text = s.toString();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionViewHolder> {
        Map<QuizQuestion, Boolean> expanded = new HashMap<>();

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new QuestionViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.quiz_edit_create_question, parent, false));

        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, final int position) {
            final QuizQuestion question = quiz.questions.get(position);
            Boolean isExpanded = expanded.get(question);
            if (isExpanded == null) {
                isExpanded = false;
            }

            holder.setQuestion(question, isExpanded);
            holder.itemView.setOnClickListener(v -> {
                Boolean cur = expanded.get(question);
                if (cur == null) {
                    cur = false;
                }
                expanded.put(question, !cur);
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return quiz.questions.size();
        }
    }

    private class QuestionViewHolder extends RecyclerView.ViewHolder {
        private QuizQuestion question;
        private TextView questionDetails;
        private QuestionViewHolder.TextListener listener;
        private TextView questionTitle;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setQuestion(final QuizQuestion question, boolean expanded) {
            this.question = question;

            RecyclerView answersList = itemView.findViewById(R.id.create_question_answer_list);
            questionTitle = itemView.findViewById(R.id.create_question_title);
            EditText questionText = itemView.findViewById(R.id.create_question_question_text);
            questionDetails = itemView.findViewById(R.id.create_question_subtitle);

            final AnswerAdapter adapter = new AnswerAdapter(question, this);

            if (listener == null) {
                listener = new QuestionViewHolder.TextListener(question);
                questionText.addTextChangedListener(listener);
            }
            listener.setQuestion(question);

            answersList.setAdapter(adapter);
            answersList.setLayoutManager(new LinearLayoutManager(getContext()));

            if (expanded) {
                itemView.findViewById(R.id.create_questions_question_details).setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.create_questions_question_details).setVisibility(View.GONE);
            }

            itemView.findViewById(R.id.create_question_add_answer).setOnClickListener(v -> {
                createAnswer();
                adapter.notifyDataSetChanged();
            });

            itemView.findViewById(R.id.create_question_delete_question).setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.delete_question_confirm_title));
                builder.setMessage(getString(R.string.delete_question_confirm));
                builder.setPositiveButton(getString(R.string.delete_question_delete_button), (dialog, id) -> {
                    deleteQuestion(question);
                    dialog.dismiss();
                });
                builder.setNegativeButton(getString(R.string.delete_question_cancel_button), (dialog, id) -> dialog.dismiss());

                AlertDialog alert = builder.create();
                alert.show();
            });


            questionTitle.setText(question.text);
            questionText.setText(question.text);

            updateAnswerDetails();
        }

        private void updateAnswerDetails() {
            int count = question.answers.size();
            questionDetails.setText(getResources().getQuantityString(R.plurals.contains_x_answers, count, count));
        }

        private void createAnswer() {
            question.answers.add(new QuizAnswer());
            System.out.println(new Gson().toJson(quiz));
            updateAnswerDetails();
        }

        private void removeAnswer(QuizAnswer answer) {
            question.answers.remove(answer);
            System.out.println(new Gson().toJson(quiz));
            updateAnswerDetails();
        }

        private class TextListener implements TextWatcher {

            private QuizQuestion question;

            public TextListener(QuizQuestion question) {
                this.question = question;
            }

            public void setQuestion(QuizQuestion question) {
                this.question = question;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String title = s.toString();
                question.text = title;
                questionTitle.setText(title);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }

    }

}
