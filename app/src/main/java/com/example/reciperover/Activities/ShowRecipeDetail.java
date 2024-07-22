package com.example.reciperover.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.reciperover.Models.RecipeDetail;
import com.example.reciperover.R;
import com.example.reciperover.databinding.ActivityShowRecipeDetailBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowRecipeDetail extends AppCompatActivity {
    ActivityShowRecipeDetailBinding binding;
    String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        docId = getIntent().getStringExtra("docID");
        if (docId != null) {
            FirebaseFirestore.getInstance()
                    .collection("Recipes")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            RecipeDetail recipeDetail = documentSnapshot.toObject(RecipeDetail.class);
                            if (recipeDetail != null) {
                                displayRecipeDetails(recipeDetail);
                            } else {
                                Log.e("ShowRecipeDetailActivity", "RecipeDetail object is null");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that may occur during the Firestore query
                        Log.e("ShowRecipeDetailActivity", "Error getting recipe details", e);
                    });
        } else {
            Log.e("ShowRecipeDetailActivity", "Document ID is null");
            // Handle the case when document ID is null, perhaps show an error message or finish the activity
            finish();
        }

        binding.bacBtn.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @SuppressLint("ResourceAsColor")
    private void displayRecipeDetails(RecipeDetail recipeDetail) {
        if (!isDestroyed() && recipeDetail != null) {
            if (recipeDetail.getRecipeImageUrl() != null) {
                Glide.with(this)
                        .load(recipeDetail.getRecipeImageUrl())
                        .into(binding.showRecipeImage);
            }
            binding.showRecipeName.setText(recipeDetail.getRecipeName());
            binding.showType.setText(recipeDetail.getRecipeType());
            binding.showTime.setText(recipeDetail.getRecipeTime());
            binding.showDiff.setText(recipeDetail.getRecipeDiff());
            loadRecipeIngredients();
            binding.ingredientsBtn.setOnClickListener(v -> {
                binding.procedure.setVisibility(View.GONE);
                binding.showIngredientsChipGroup.setVisibility(View.VISIBLE); 
            });
            binding.procedureBtn.setOnClickListener(v -> {
                binding.showIngredientsChipGroup.setVisibility(View.GONE);
                binding.procedure.setVisibility(View.VISIBLE);
                binding.procedure.setText(recipeDetail.getRecipeDescr());
            });
        }else {
            Log.e("ShowRecipeDetailActivity", "RecipeDetail object is null");
        }
    }
    private void loadRecipeIngredients() {
//        Intent intent = getIntent();
//        RecipeDetail selectedRecipe = (RecipeDetail) intent.getSerializableExtra("Selected_Recipe");
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Recipes").document(docId);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    List<String> ingredientList = new ArrayList<>();
                    List<String> ingredients = (List<String>) documentSnapshot.get("recipeIngrdnts");
                    if (ingredients != null) {
                        ingredientList.addAll(ingredients);
                        setUpChipGroup(ingredientList);
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    // Handle errors
                    Log.w("Firestore", "Error getting document", task.getException());
                }
            }

        });
    }
    private void setUpChipGroup(List<String> ingredientList){
        for (String ingredient : ingredientList) {
            Chip chip = new Chip(ShowRecipeDetail.this);
            chip.setText(ingredient);
            binding.showIngredientsChipGroup.addView(chip);
        }
    }
}
