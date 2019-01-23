package me.srikavin.quiz.view

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Picasso
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.viewmodel.BattleViewModel
import java.util.*

class BattleFragment : Fragment() {
    private var mViewModel: BattleViewModel? = null
    private var adapter: QuizListAdapter? = null

    private var swipeRefresh: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val recyclerView = view!!.findViewById<RecyclerView>(R.id.battle_quizzes_recycler_view)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        adapter = QuizListAdapter(this.context!!)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val halfSpace = 8

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

                if (parent.paddingLeft != halfSpace) {
                    parent.setPadding(halfSpace, halfSpace, halfSpace, halfSpace)
                    parent.clipToPadding = true
                }

                outRect.top = halfSpace
                outRect.bottom = halfSpace
                outRect.left = halfSpace
                outRect.right = halfSpace
            }
        })

        val testing = ArrayList<Quiz>()
        testing.add(object : Quiz() {
            init {
                title = "FBLA Testing: Coding & Programming"
                coverImage = "https://images.unsplash.com/photo-1535498730771-e735b998cd64?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&w=1000&q=80"
            }
        })
        testing.add(object : Quiz() {
            init {
                title = "FBLA Dresscode"
                coverImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQd249c-y8zHGhkBuFy2M3bcl_G9WGsJbThLGLOI0OK9GScvCeO3g"
            }
        })
        testing.add(object : Quiz() {
            init {
                title = "FBLA Testing: Computer Problem Solving"
                coverImage = "https://images.pexels.com/photos/754082/pexels-photo-754082.jpeg?cs=srgb&dl=blur-blurred-background-colors-754082.jpg&fm=jpg"
            }
        })
        testing.add(object : Quiz() {
            init {
                title = "FBLA Testing: Mobile App Development"
                coverImage = "https://images.pexels.com/photos/754082/pexels-photo-754082.jpeg?cs=srgb&dl=blur-blurred-background-colors-754082.jpg&fm=jpg"
            }
        })
        testing.add(object : Quiz() {
            init {
                title = "asd2412123"
                coverImage = "https://i.pinimg.com/736x/76/3c/f2/763cf2372ce3775ff4956549fd664455.jpg"
            }
        })

        adapter!!.setQuizzes(testing)

        mViewModel = ViewModelProviders.of(this).get(BattleViewModel::class.java)
        mViewModel!!.quizzes.observe(this, Observer { quizzes ->
            if (quizzes != null) {
                adapter!!.setQuizzes(quizzes)
            } else {
                Toast.makeText(context, R.string.data_load_fail, Toast.LENGTH_LONG).show()
            }
            swipeRefresh!!.isRefreshing = false
        })

        swipeRefresh = view!!.findViewById(R.id.battle_swipe_refresh)
        swipeRefresh!!.setOnRefreshListener { this.updateQuizzes() }
        context
    }

    private fun updateQuizzes() {
        mViewModel!!.updateQuizzes()
    }

    internal class QuizListAdapter(private val context: Context) : RecyclerView.Adapter<QuizListViewHolder>() {
        private var quizzes: List<Quiz>? = null

        init {
            this.quizzes = ArrayList()
        }

        fun setQuizzes(quizzes: List<Quiz>?) {
            this.quizzes = quizzes
            this.notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizListViewHolder {
            return QuizListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.battle_grid_item, parent, false))
        }

        override fun onBindViewHolder(holder: QuizListViewHolder, position: Int) {
            holder.setQuiz(quizzes!![position])
            holder.container.setOnClickListener { v ->
                val quiz = quizzes!![position]
                Toast.makeText(holder.container.context, quizzes!![position].title, Toast.LENGTH_LONG).show()


                val intent = Intent(context, QuizDetail::class.java)
                intent.putExtra("id", quiz.id)
                context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return quizzes!!.size
        }
    }

    internal class QuizListViewHolder(val container: View) : RecyclerView.ViewHolder(container) {
        private var quiz: Quiz? = null
        private val image: ImageView
        private val title: TextView

        init {
            image = container.findViewById(R.id.battle_grid_item_image)
            title = container.findViewById(R.id.battle_grid_item_title)
        }

        fun setQuiz(quiz: Quiz) {
            this.quiz = quiz
            this.title.text = quiz.title
            println(quiz.coverImage)
            Picasso.get().load(quiz.coverImage).into(image)
        }
    }
}
