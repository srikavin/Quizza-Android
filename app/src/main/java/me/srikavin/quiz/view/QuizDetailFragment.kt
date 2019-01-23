package me.srikavin.quiz.view

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
import me.srikavin.quiz.view.game.GameActivity
import me.srikavin.quiz.viewmodel.QuizDetailViewModel

class QuizDetailFragment : Fragment() {

    private var mViewModel: QuizDetailViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quiz_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val toolbar = view!!.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mViewModel = ViewModelProviders.of(this).get(QuizDetailViewModel::class.java)

        val collapsingToolbar = view!!.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbar.title = "title testing"
        Picasso.get().load(null as String?)
                .placeholder(ColorDrawable(resources.getColor(R.color.colorSecondaryLight, null)))
                .into(view!!.findViewById<View>(R.id.image) as ImageView)

        val id = arguments!!.getString("id")!!


        mViewModel!!.getQuizByID(id).observe(this, Observer { quiz ->
            if (quiz != null) {
                collapsingToolbar.title = quiz.title
                if (quiz.coverImage != null) {
                    Picasso.get().load(quiz.coverImage).into(view!!.findViewById<View>(R.id.image) as ImageView)
                }
                val textView = view!!.findViewById<TextView>(R.id.quiz_detail_description)
                textView.text = if (quiz.description == null) "" else quiz.description
            } else {
                Toast.makeText(context, R.string.data_load_fail, Toast.LENGTH_LONG).show()
            }
        })

        view!!.findViewById<View>(R.id.quiz_detail_battle_fab).setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)
            intent.putExtra("id", id)
            activity!!.startActivity(intent)
        }
    }

    companion object {
        fun newInstance(id: String): QuizDetailFragment {
            val fragment = QuizDetailFragment()
            val args = Bundle()
            args.putString("id", id)
            fragment.arguments = args
            return fragment
        }
    }

}
