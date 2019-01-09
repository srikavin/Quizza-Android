package me.srikavin.quiz.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.viewmodel.CreateViewModel;

public class CreateFragment extends Fragment {
    public final static int RECV_CREATE_QUIZ = 1;
    public final static int RECV_EDIT_QUIZ = 2;

    private CreateViewModel viewModel;
    private View noQuizOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(CreateViewModel.class);

        getView().findViewById(R.id.create_question_button).setOnClickListener(v -> createNewQuiz());

        getView().findViewById(R.id.create_fab).setOnClickListener(v -> createNewQuiz());

        final QuizAdapter adapter = new QuizAdapter();

        RecyclerView draftList = getView().findViewById(R.id.create_draft_recycler);
        draftList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        draftList.setAdapter(adapter);

        viewModel.getOwnedQuizzes().observe(this, quizzes -> {
            updateNoQuizOverlay(quizzes);
            adapter.setData(quizzes);
            adapter.notifyDataSetChanged();
        });
        noQuizOverlay = getView().findViewById(R.id.create_no_quiz_overlay);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void update() {
        viewModel.updateDrafts();
    }

    private void deleteQuiz(Quiz quiz) {
        viewModel.deleteQuiz(quiz);
    }

    private void editQuiz(Quiz quiz) {
        assert getContext() != null;
        assert getActivity() != null;
        Intent intent = new Intent(getContext(), QuizEditActivity.class);
        intent.putExtra("mode", QuizEditFragment.Mode.EDIT);
        intent.putExtra("id", quiz.id);
        getActivity().startActivityForResult(intent, RECV_EDIT_QUIZ);

    }

    private void createNewQuiz() {
        assert getContext() != null;
        Intent intent = new Intent(getContext(), QuizEditActivity.class);
        intent.putExtra("mode", QuizEditFragment.Mode.CREATE);
        getActivity().startActivityForResult(intent, RECV_CREATE_QUIZ);
    }

    private void updateNoQuizOverlay(List<Quiz> quizzes) {
        noQuizOverlay.setVisibility(quizzes.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private class QuizAdapter extends RecyclerView.Adapter<QuizViewHolder> {
        Map<Quiz, Boolean> expanded = new HashMap<>();
        private List<Quiz> quizzes = new ArrayList<>();

        @NonNull
        @Override
        public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new QuizViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.create_quiz_list_item, parent, false));

        }

        @Override
        public void onBindViewHolder(@NonNull QuizViewHolder holder, final int position) {
            final Quiz quiz = quizzes.get(position);
            Boolean isExpanded = expanded.get(quiz);
            if (isExpanded == null) {
                isExpanded = false;
            }

            holder.setQuiz(quiz, isExpanded);
            holder.itemView.setOnClickListener(v -> {
                Boolean cur = expanded.get(quiz);
                if (cur == null) {
                    cur = false;
                }
                expanded.put(quiz, !cur);
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return quizzes.size();
        }

        void setData(List<Quiz> quizzes) {
            this.quizzes = quizzes;
        }
    }

    private class QuizViewHolder extends RecyclerView.ViewHolder {

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setQuiz(final Quiz quiz, boolean expanded) {
            TextView quizTitle = itemView.findViewById(R.id.create_quiz_title);
            TextView quizSubtitle = itemView.findViewById(R.id.create_quiz_subtitle);
            TextView quizStatus = itemView.findViewById(R.id.create_quiz_status);

            quizTitle.setText(quiz.title);
            if (quiz.draft) {
                quizStatus.setText(getString(R.string.create_quiz_draft));
                quizStatus.setTextColor(getResources().getColor(R.color.colorSecondaryDark, null));
            } else {
                quizStatus.setText(getString(R.string.create_quiz_published));
                quizStatus.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            }
            int questions = quiz.questions.size();
            quizSubtitle.setText(getResources().getQuantityString(R.plurals.contains_x_questions, questions, questions));

            if (expanded) {
                itemView.findViewById(R.id.create_quiz_details).setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.create_quiz_details).setVisibility(View.GONE);
            }

            itemView.findViewById(R.id.create_quiz_delete_quiz).setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.delete_quiz_title));
                builder.setMessage(getString(R.string.delete_quiz_confirmm));
                builder.setPositiveButton("Delete Quiz", (dialog, id) -> {
                    deleteQuiz(quiz);
                    dialog.dismiss();
                });
                builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

                AlertDialog alert = builder.create();
                alert.show();
            });
            itemView.findViewById(R.id.create_quiz_edit_quiz).setOnClickListener(v -> editQuiz(quiz));
        }
    }
}
