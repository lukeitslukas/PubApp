package com.ratemypub.PubApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
    }

    public boolean validate(String email, String password, String password_confirm) {
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
        String email = ((EditText) findViewById(R.id.register_email)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.register_password)).getText().toString().trim();
        String password_confirm = ((EditText) findViewById(R.id.register_password_confirm)).getText().toString().trim();

        if(validate(email, password, password_confirm)){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
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
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+!])\\S{4,20}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}