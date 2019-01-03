package me.srikavin.quiz.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.srikavin.quiz.R;

public class CreateFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().findViewById(R.id.create_question_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewQuiz();
            }
        });

        getView().findViewById(R.id.create_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewQuiz();
            }
        });
    }

    private void createNewQuiz() {
        assert getContext() != null;
        Intent intent = new Intent(getContext(), QuizEditActivity.class);
        intent.putExtra("mode", QuizEditFragment.Mode.CREATE);
        getContext().startActivity(intent);
    }
}
