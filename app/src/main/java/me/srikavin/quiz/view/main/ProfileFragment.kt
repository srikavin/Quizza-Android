package me.srikavin.quiz.view.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import me.srikavin.quiz.R
import me.srikavin.quiz.viewmodel.ProfileAction
import me.srikavin.quiz.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var username: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, ProfileViewModelFactory()).get(ProfileViewModel::class.java)

        viewModel.dispatch(ProfileAction.Load)

        username = view.findViewById(R.id.profile_username)

        viewModel.observableState.observe(viewLifecycleOwner, Observer {
            if (it.user != null) {
                username.text = it.user.username
            } else {
                username.text = getString(R.string.offline_user_name)
            }
        })

        view.findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            viewModel.dispatch(ProfileAction.Logout)

            if (context != null) {
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
