package me.srikavin.quiz.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.srikavin.quiz.R;

public class LoginActivity extends AppCompatActivity {
    private final String LOGIN_FRAGMENT_TAG = "login_fragment_a76d2a";
    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        if (savedInstanceState == null) {
            loginFragment = LoginFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, loginFragment, LOGIN_FRAGMENT_TAG)
                    .commitNow();
        } else {
            loginFragment = ((LoginFragment) getSupportFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginFragment.GOOGLE_LOGIN_RC) {
            loginFragment.handleGoogleSignIn(data);
        }
    }
}
