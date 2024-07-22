package com.example.reciperover.Activities;

import static com.example.reciperover.R.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.reciperover.R;
import com.google.firebase.auth.FirebaseAuth;

public class Splash extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth=FirebaseAuth.getInstance();
        new Handler().postDelayed(() -> {
            // Check if the user is logged in......
            if (mAuth.getCurrentUser() != null) {
                navigateToHome();
            } else {
                startLoginActivity();
            }
        }, 2000);

    }
    // if user is logged in this method will be run!
    private void navigateToHome(){
        startActivity(new Intent(Splash.this,Home.class));
        finish();
    }
    // if user not logged in then this method will be run!
    private void startLoginActivity() {
        startActivity(new Intent(Splash.this, Login.class));
        finish();
    }
}