package me.srikavin.quiz.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.Quiz;
import me.srikavin.quiz.viewmodel.BattleViewModel;

public class BattleFragment extends Fragment {
    private BattleViewModel mViewModel;
    private QuizListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_battle, container, false);
    }

    private SwipeRefreshLayout swipeRefresh;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView recyclerView = getView().findViewById(R.id.battle_quizzes_recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        adapter = new QuizListAdapter(this.getContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            private int halfSpace = 8;

            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

                if (parent.getPaddingLeft() != halfSpace) {
                    parent.setPadding(halfSpace, halfSpace, halfSpace, halfSpace);
                    parent.setClipToPadding(true);
                }

                outRect.top = halfSpace;
                outRect.bottom = halfSpace;
                outRect.left = halfSpace;
                outRect.right = halfSpace;
            }
        });

        List<Quiz> testing = new ArrayList<>();
        testing.add(new Quiz() {{
            title = "FBLA Testing: Coding & Programming";
            coverImageUrl = "https://images.unsplash.com/photo-1535498730771-e735b998cd64?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&w=1000&q=80";
        }});
        testing.add(new Quiz() {{
            title = "FBLA Dresscode";
            coverImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQd249c-y8zHGhkBuFy2M3bcl_G9WGsJbThLGLOI0OK9GScvCeO3g";
        }});
        testing.add(new Quiz() {{
            title = "FBLA Testing: Computer Problem Solving";
            coverImageUrl = "https://images.pexels.com/photos/754082/pexels-photo-754082.jpeg?cs=srgb&dl=blur-blurred-background-colors-754082.jpg&fm=jpg";
        }});
        testing.add(new Quiz() {{
            title = "FBLA Testing: Mobile App Development";
            coverImageUrl = "https://images.pexels.com/photos/754082/pexels-photo-754082.jpeg?cs=srgb&dl=blur-blurred-background-colors-754082.jpg&fm=jpg";
        }});
        testing.add(new Quiz() {{
            title = "asd2412123";
            coverImageUrl = "https://i.pinimg.com/736x/76/3c/f2/763cf2372ce3775ff4956549fd664455.jpg";
        }});

        adapter.setQuizzes(testing);

        mViewModel = ViewModelProviders.of(this).get(BattleViewModel.class);
        mViewModel.getQuizzes().observe(this, new Observer<List<Quiz>>() {
            @Override
            public void onChanged(@Nullable List<Quiz> quizzes) {
                if (quizzes != null) {
                    adapter.setQuizzes(quizzes);
                } else {
                    Toast.makeText(getContext(), R.string.data_load_fail, Toast.LENGTH_LONG).show();
                }
                swipeRefresh.setRefreshing(false);
            }
        });

        swipeRefresh = getView().findViewById(R.id.battle_swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateQuizzes();
            }
        });
        getContext();
    }

    private void updateQuizzes() {
        mViewModel.updateQuizzes();
    }

    static class QuizListAdapter extends RecyclerView.Adapter<QuizListViewHolder> {
        private List<Quiz> quizzes;
        private Context context;

        public QuizListAdapter(Context context) {
            this.context = context;
            this.quizzes = new ArrayList<>();
        }

        public void setQuizzes(List<Quiz> quizzes) {
            this.quizzes = quizzes;
            this.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public QuizListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new QuizListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.battle_grid_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final QuizListViewHolder holder, final int position) {
            holder.setQuiz(quizzes.get(position));
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Quiz quiz = quizzes.get(position);
                    Toast.makeText(holder.container.getContext(), quizzes.get(position).title, Toast.LENGTH_LONG).show();


                    Intent intent = new Intent(context, QuizDetail.class);
                    intent.putExtra("id", quiz.id);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return quizzes.size();
        }
    }

    static class QuizListViewHolder extends RecyclerView.ViewHolder {
        private Quiz quiz;
        private ImageView image;
        private TextView title;
        private View container;

        public QuizListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.container = itemView;
            image = itemView.findViewById(R.id.battle_grid_item_image);
            title = itemView.findViewById(R.id.battle_grid_item_title);
        }

        public View getContainer() {
            return container;
        }

        public void setQuiz(Quiz quiz) {
            this.quiz = quiz;
            this.title.setText(quiz.title);
            Picasso.get().load(quiz.coverImageUrl).into(image);
        }
    }
}
