package com.example.reciperover.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.reciperover.R;
import com.example.reciperover.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfile extends AppCompatActivity {
    ActivityEditProfileBinding binding;
    String newName;
    FirebaseUser fUser;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    DocumentReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()){
                    changeName();
                }
            }
        });
        binding.back.setOnClickListener(v -> {onBackPressed();});
    }
    private boolean isValid(){
        newName = binding.changeName.getText().toString().trim();
        if (newName.length() < 3){
            Toast.makeText(getApplicationContext(), "Enter a Full Username", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void changeName(){
        documentReference = db.collection("Users").document(fUser.getUid());
        documentReference.update("name",newName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"Name Updated",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to update name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}