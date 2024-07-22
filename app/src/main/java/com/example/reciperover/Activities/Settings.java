package com.example.reciperover.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.reciperover.R;
import com.google.firebase.auth.FirebaseAuth;

public class Settings extends AppCompatActivity {
    com.example.reciperover.databinding.ActivitySettingsBinding binding;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.reciperover.databinding.ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        mAuth = FirebaseAuth.getInstance();
        binding.logoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLogoutDialog();
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.changeNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this,EditProfile.class));
            }
        });
        binding.changePassLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this, ChangePassword.class));
                //finish();
            }
        });

    }
    private void openLogoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
        builder.setTitle("Are you sure you want to logout?");
        String [] options = {"Yes","No"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        mAuth.signOut();
                        Intent intent= new Intent(Settings.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                        break;
                    case 1:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }
}