package me.srikavin.quiz.view;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import me.srikavin.quiz.R;
import me.srikavin.quiz.view.game.GameActivity;
import me.srikavin.quiz.viewmodel.QuizDetailViewModel;

public class QuizDetailFragment extends Fragment {

    private QuizDetailViewModel mViewModel;

    public static QuizDetailFragment newInstance(String id) {
        QuizDetailFragment fragment = new QuizDetailFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quiz_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getView().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewModel = ViewModelProviders.of(this).get(QuizDetailViewModel.class);

        final CollapsingToolbarLayout collapsingToolbar = getView().findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("title testing");
        Picasso.get().load((String) null)
                .placeholder(new ColorDrawable(getResources().getColor(R.color.colorSecondaryLight, null)))
                .into((ImageView) getView().findViewById(R.id.image));

        assert getArguments() != null;

        String id = getArguments().getString("id");
        assert id != null;


        mViewModel.getQuizByID(id).observe(this, quiz -> {
            if (quiz != null) {
                collapsingToolbar.setTitle(quiz.title);
                if (quiz.coverImage != null) {
                    Picasso.get().load(quiz.coverImage).into((ImageView) getView().findViewById(R.id.image));
                }
                TextView textView = getView().findViewById(R.id.quiz_detail_description);
                textView.setText(quiz.description == null ? "" : quiz.description);
            } else {
                Toast.makeText(getContext(), R.string.data_load_fail, Toast.LENGTH_LONG).show();
            }
        });

        getView().findViewById(R.id.quiz_detail_battle_fab).setOnClickListener((v) -> {
            Intent intent = new Intent(getActivity(), GameActivity.class);
            intent.putExtra("id", id);
            getActivity().startActivity(intent);
        });
    }

}
