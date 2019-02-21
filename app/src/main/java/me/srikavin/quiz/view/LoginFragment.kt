package me.srikavin.quiz.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import me.srikavin.quiz.R
import me.srikavin.quiz.model.AuthUser
import me.srikavin.quiz.repository.AuthRepository
import me.srikavin.quiz.repository.error.ErrorWrapper
import me.srikavin.quiz.view.main.MainActivity
import me.srikavin.quiz.viewmodel.LoginViewModel

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        viewModel.verifyAuth().observe(this,
                Observer { verified ->
                    activity!!.runOnUiThread {
                        if (verified!!) {
                            Toast.makeText(activity, "Signed in with saved credentials", Toast.LENGTH_SHORT).show()
                            continueToMain()
                        } else {
                            Toast.makeText(activity, "Failed to sign in with saved credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

        val username = view!!.findViewById<TextInputLayout>(R.id.login_activity_username_view)
        val password = view!!.findViewById<TextInputLayout>(R.id.login_activity_password_view)
        val loginButton = view!!.findViewById<Button>(R.id.login_activity_login_button)
        val registerButton = view!!.findViewById<Button>(R.id.login_activity_register_button)
        val loginOffline = view!!.findViewById<Button>(R.id.login_offline)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()

        val googleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)
        val account = GoogleSignIn.getLastSignedInAccount(this.context!!)
        println(account)

        val signInButton = view!!.findViewById<SignInButton>(R.id.google_sign_in)

        signInButton.setOnClickListener {
            //Handle google sign-in
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_LOGIN_RC)
        }

        viewModel.authUser.observe(
                viewLifecycleOwner,
                Observer<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>> { this@LoginFragment.handleAuthResult(it) }
        )

        loginButton.setOnClickListener {
            val usernameString = username.editText!!.text.toString()
            val passwordString = password.editText!!.text.toString()
            viewModel.login(usernameString, passwordString)
        }

        registerButton.setOnClickListener {
            val usernameString = username.editText!!.text.toString()
            val passwordString = password.editText!!.text.toString()
            viewModel.register(usernameString, passwordString)
        }

        loginOffline.setOnClickListener {
            AuthRepository.setAuthToken(requireContext(), "offline")
            Toast.makeText(context, "Offline login successful", Toast.LENGTH_SHORT).show()
            continueToMain()
        }

    }

    /**
     * Handles the display and redirection to another activity after a login attempt.
     */
    private fun handleAuthResult(wrapper: ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>) {
        if (wrapper.hasErrors()) {
            for (e in wrapper.errors!!) {
                val message: String = when (e) {
                    AuthRepository.ErrorCodes.USERNAME_OR_PASSWORD_INCORRECT -> getString(R.string.error_username_or_password_incorrect)
                    AuthRepository.ErrorCodes.USERNAME_INVALID -> getString(R.string.error_username_invalid)
                    AuthRepository.ErrorCodes.USERNAME_TAKEN -> getString(R.string.error_username_taken)
                    AuthRepository.ErrorCodes.PASSWORD_INVALID -> getString(R.string.error_password_invalid)
                    AuthRepository.ErrorCodes.NETWORK_ERROR -> getString(R.string.error_network)
                    AuthRepository.ErrorCodes.SERVER_ERROR -> getString(R.string.error_server)
                    AuthRepository.ErrorCodes.UNKNOWN_ERROR -> getString(R.string.error_unknown)
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            return
        }

        Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
        continueToMain()
    }

    private fun continueToMain() {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("auth", true)
        startActivity(intent)
    }

    fun handleGoogleSignIn(data: Intent?) {
        println("LoginFragment.handleGoogleSignIn")
        println("data = [${data}]")
        handleGoogleSignIn(GoogleSignIn.getSignedInAccountFromIntent(data))
    }

    private fun handleGoogleSignIn(account: Task<GoogleSignInAccount>) {
        try {
            val token = account.getResult(ApiException::class.java)!!.idToken

            if (token == null) {
                Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                return
            }

            viewModel.loginGoogleAuth(token)
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    companion object {
        const val GOOGLE_LOGIN_RC = 9001

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

}
