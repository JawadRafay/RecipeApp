package com.example.reciperover.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.reciperover.R;
import com.example.reciperover.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    ActivityForgotPasswordBinding binding;
    String strEmail;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        fAuth = FirebaseAuth.getInstance();
        binding.resetCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strEmail = binding.resetEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(strEmail)){
                    resetPassword();
                }else {
                    binding.resetEmail.setError("Email field can't be empty");
                }
            }
        });
        binding.backCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPassword.this, Login.class));
                finish();
            }
        });

    }
    private void resetPassword(){
        binding.forgotProgress.setVisibility(View.VISIBLE);
        binding.resetCard.setVisibility(View.INVISIBLE);
        fAuth.sendPasswordResetEmail(strEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText
                        (ForgotPassword.this,"Reset password link has been sent to your email",
                                Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ForgotPassword.this, Login.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText
                        (ForgotPassword.this,"Error :-"+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                binding.forgotProgress.setVisibility(View.INVISIBLE);
                binding.resetCard.setVisibility(View.VISIBLE);
            }
        });
    }
}