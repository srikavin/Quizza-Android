package me.srikavin.quiz.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.srikavin.quiz.R

class LoginActivity : AppCompatActivity() {
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        if (savedInstanceState == null) {
            loginFragment = LoginFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, loginFragment, LOGIN_FRAGMENT_TAG)
                    .commitNow()
        } else {
            loginFragment = supportFragmentManager.findFragmentByTag(LOGIN_FRAGMENT_TAG) as LoginFragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("requestCode = [${requestCode}], resultCode = [${resultCode}], data = [${data?.extras}]")
//        if (requestCode == LoginFragment.GOOGLE_LOGIN_RC) {
        println("LoginActivity.onActivityResult")
        loginFragment.handleGoogleSignIn(data)
//        }
    }

    companion object {
        private const val LOGIN_FRAGMENT_TAG = "login_fragment_a76d2a"
    }
}
