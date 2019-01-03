package me.srikavin.quiz.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.AuthUser;
import me.srikavin.quiz.repository.AuthRepository;
import me.srikavin.quiz.repository.error.ErrorWrapper;
import me.srikavin.quiz.viewmodel.LoginViewModel;

public class LoginFragment extends Fragment {

    private LoginViewModel mViewModel;
    public static final int GOOGLE_LOGIN_RC = 1000;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        final TextInputLayout username = getView().findViewById(R.id.login_activity_username_view);
        final TextInputLayout password = getView().findViewById(R.id.login_activity_password_view);
        Button loginButton = getView().findViewById(R.id.login_activity_login_button);
        Button registerButton = getView().findViewById(R.id.login_activity_register_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();

        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this.getActivity(), gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getContext());
        System.out.println(account);

        SignInButton signInButton = getView().findViewById(R.id.google_sign_in);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle google sign-in
                if (v.getId() == R.id.google_sign_in) {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, GOOGLE_LOGIN_RC);
                }
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getEditText().getText().toString();
                String passwordString = password.getEditText().getText().toString();
                mViewModel.login(usernameString, passwordString)
                        .observe(getViewLifecycleOwner(), new Observer<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>>() {
                            @Override
                            public void onChanged(ErrorWrapper<AuthUser, AuthRepository.ErrorCodes> authWrapper) {
                                handleAuthResult(authWrapper);
                            }
                        });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getEditText().getText().toString();
                String passwordString = password.getEditText().getText().toString();
                mViewModel.register(usernameString, passwordString)
                        .observe(getViewLifecycleOwner(), new Observer<ErrorWrapper<AuthUser, AuthRepository.ErrorCodes>>() {
                            @Override
                            public void onChanged(ErrorWrapper<AuthUser, AuthRepository.ErrorCodes> authWrapper) {
                                handleAuthResult(authWrapper);
                            }
                        });
            }
        });
    }

    private void handleAuthResult(ErrorWrapper<AuthUser, AuthRepository.ErrorCodes> wrapper) {
        if (wrapper.hasErrors()) {
            for (AuthRepository.ErrorCodes e : wrapper.getErrors()) {
                String message;
                switch (e) {
                    case USERNAME_OR_PASSWORD_INCORRECT:
                        message = getString(R.string.error_username_or_password_incorrect);
                        break;
                    case USERNAME_INVALID:
                        message = getString(R.string.error_username_invalid);
                        break;
                    case USERNAME_TAKEN:
                        message = getString(R.string.error_username_taken);
                        break;
                    case PASSWORD_INVALID:
                        message = getString(R.string.error_password_invalid);
                        break;
                    case NETWORK_ERROR:
                        message = getString(R.string.error_network);
                        break;
                    case SERVER_ERROR:
                        message = getString(R.string.error_server);
                        break;
                    case UNKNOWN_ERROR:
                    default:
                        message = getString(R.string.error_unknown);
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Toast.makeText(getContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(getContext(), MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
    }

    public void handleGoogleSignIn(Intent data) {
        handleGoogleSignIn(GoogleSignIn.getSignedInAccountFromIntent(data));
    }

    public void handleGoogleSignIn(Task<GoogleSignInAccount> account) {
        try {
            System.out.println(account.getResult(ApiException.class).getIdToken());
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Google sign-in failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
