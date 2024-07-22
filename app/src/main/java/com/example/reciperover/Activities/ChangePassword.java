package com.example.reciperover.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.reciperover.R;
import com.example.reciperover.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePassword extends AppCompatActivity {
    ActivityChangePasswordBinding binding;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        binding.savePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPass = binding.currentPass.getText().toString().trim();
                String newPass = binding.newPass.getText().toString().trim();
                if (TextUtils.isEmpty(currentPass)){
                    Toast.makeText(getApplicationContext(),"Enter Current Password",Toast.LENGTH_SHORT);
                    return;
                }
                if (newPass.length() <6){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 characters long!",Toast.LENGTH_SHORT);
                    return;
                }
                changePassword(currentPass,newPass);

            }
        });
        binding.backBTn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
    private void changePassword(String currentPass,String newPass){
        binding.changePassProgress.setVisibility(View.VISIBLE);
        // --------Before changing password re-authenticate the user-----
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(),currentPass);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Successfully Authenticated, Begin Update
                        user.updatePassword(newPass)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Password updated
                                        binding.changePassProgress.setVisibility(View.INVISIBLE);
                                        Toast.makeText(ChangePassword.this, "Password updated! Now you can login with a new password",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        binding.changePassProgress.setVisibility(View.INVISIBLE);
                                        Toast.makeText(ChangePassword.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChangePassword.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.changePassProgress.setVisibility(View.INVISIBLE);
                    }
                });
    }
}