package com.ratemypub.PubApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private static final int REQ_ONE_TAP = 3;
    private boolean showOneTapUI = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId("618107406085-98gq3tuqtipgneql83j20vvk854hn5ok.apps.googleusercontent.com")
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if back out of activity return to login fragment
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean validate(String email, String password, String password_confirm) {
        // validate entered information
        if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Invalid Email Address",
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(RegisterActivity.this, email,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(!isValidPassword(password)) {
            Toast.makeText(RegisterActivity.this, password,
                    Toast.LENGTH_SHORT).show();
            Toast.makeText(RegisterActivity.this, "fart",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, password,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Password field empty",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(password_confirm)) {
            Toast.makeText(RegisterActivity.this, "Confirm Password field empty",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if(!password.equals(password_confirm)){
            //Toast.makeText(RegisterActivity.this, "Passwords do not match",
                    //Toast.LENGTH_SHORT).show();
            Toast.makeText(RegisterActivity.this, password, Toast.LENGTH_SHORT).show();
            Toast.makeText(RegisterActivity.this, password_confirm, Toast.LENGTH_SHORT).show();
                return false;
        } else {
            return true;
        }
    }

    public void createAccount(View view) {
        // get user entered information
        String email = ((EditText) findViewById(R.id.register_email)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.register_password)).getText().toString().trim();
        String password_confirm = ((EditText) findViewById(R.id.register_password_confirm)).getText().toString().trim();

        // validate entered info, push to db and login
        if(validate(email, password, password_confirm)){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public boolean isValidPassword(String password)
    {
        // regex test if password is valid
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+!])\\S{4,20}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public void loginGoogle(View view) {
        oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(RegisterActivity.this,
                                    "Couldn't start One Tap UI: " + e.getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this,
                                Objects.requireNonNull(e.getLocalizedMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    if (idToken !=  null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with your backend.
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        mAuth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            finish();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(RegisterActivity.this,
                                                    String.valueOf(task.getException()),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case CommonStatusCodes.CANCELED:
                            // Don't re-prompt the user.
                            showOneTapUI = false;
                            break;
                        default:
                            Toast.makeText(RegisterActivity.this,
                                    Objects.requireNonNull(e.getLocalizedMessage()),
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                break;
        }
    }

}