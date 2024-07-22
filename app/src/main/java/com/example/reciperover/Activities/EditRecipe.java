package com.example.reciperover.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.reciperover.Models.RecipeDetail;
import com.example.reciperover.R;
import com.example.reciperover.databinding.ActivityEditRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRecipe extends AppCompatActivity {

    ActivityEditRecipeBinding binding;
    String[] recipesType = {"Pakistani", "Indian", "Italian"};
    ArrayAdapter<String> arrayRecipesType;
    FirebaseFirestore fireStore;
    FirebaseAuth fAuth;
    private Uri imageUri;
    private FirebaseStorage storage;
    private String updatedRecipeImageUrl, updatedRecipeName, updatedRecipeType, updatedRecipeTime, updatedRecipeDifficulty,
            updatedRecipeDescription;
    private int updatedSelectedRadioBtnID;
    RecipeDetail selectedRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditRecipeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        fAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
        selectedRecipe = (RecipeDetail) getIntent().getSerializableExtra("Selected_Recipe");
        if (selectedRecipe!= null){
            loadRecipeData();
        }
        arrayRecipesType = new ArrayAdapter<String>(this, R.layout.item_auto_complete, recipesType);
        binding.edtRecipeType.setAdapter(arrayRecipesType);
        binding.edtRecipeImg.setOnClickListener(v -> {
            openGallery();
        });
        binding.bacBtn.setOnClickListener(v -> onBackPressed());
        //Chips ==============================================================================================================================
        binding.addIngredientsBtn.setOnClickListener(v -> {
            String newIngredients = binding.edtIngredients.getText().toString().trim();
            if (!newIngredients.isEmpty()) {
                addNewChip(newIngredients);
                binding.edtIngredients.getText().clear();
            }
        });
        // update recipe===============================================================================================================
        binding.editRecipeBtn.setOnClickListener(v -> {
            updateRecipe();
        });
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::onActivityResult);

    private void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            imageUri = result.getData().getData();
            Glide.with(this).load(imageUri).into(binding.edtRecipeImg);
        }
    }

    private void setRecipeType(String recipeType) {
        if (binding.edtRecipeType.getText().toString().isEmpty()) {
            binding.edtRecipeType.setText(recipeType);
        }
        binding.edtRecipeType.setAdapter(arrayRecipesType);
    }

    private void loadRecipeDifficulty() {
        DocumentReference documentReference = fireStore.collection("Recipes").document(selectedRecipe.getRecipeDocID());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String difficulty = value.getString("recipeDiff");
                if (difficulty.equalsIgnoreCase("Easy")) {
                    binding.radioEasy.setChecked(true);
                } else if (difficulty.equalsIgnoreCase("Moderate")) {
                    binding.radioModerate.setChecked(true);
                } else {
                    binding.radioHard.setChecked(true);
                }
            }
        });
    }


    private void setUpChipGroup(List<String> ingredientList) {
       List<String> copyList = new ArrayList<>(ingredientList);
        for (String ingredient : copyList) {
            Chip chip = createChip(ingredient);
            binding.edtChipGroupIngredients.addView(chip);
        }
    }

    private Chip createChip(String text)
    {
        Chip chip = new Chip(EditRecipe.this);
        selectedRecipe.getRecipeIngrdnts().add(text);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            removeChip(chip);
            selectedRecipe.getRecipeIngrdnts().remove(chip.getChipText());
        });
        return chip;
    }

    private void addNewChip(String text) {
        if (selectedRecipe.getRecipeIngrdnts().contains(text)){
            Toast.makeText(this, text + " already in ingredients", Toast.LENGTH_SHORT).show();
        }
        else {
                Chip chip = createChip(text);
                binding.edtChipGroupIngredients.addView(chip);
        }
    }

    private void removeChip(Chip chip) {
        int index = selectedRecipe.getRecipeIngrdnts().indexOf(chip.getText().toString());
        binding.edtChipGroupIngredients.removeView(chip);
        if (index != -1) {
            selectedRecipe.getRecipeIngrdnts().remove(index);
        }
//        selectedRecipe.getRecipeIngrdnts().remove(chip.getText().toString());
    }

    private void loadRecipeData() {

            if (selectedRecipe != null) {
                Glide.with(this).load(selectedRecipe.getRecipeImageUrl()).into(binding.edtRecipeImg);
                binding.newRecipeName.setText(selectedRecipe.getRecipeName());
                binding.edtRecipeType.setText(selectedRecipe.getRecipeType());
                setRecipeType(selectedRecipe.getRecipeType());
                binding.newCookingTime.setText(selectedRecipe.getRecipeTime());
                if (selectedRecipe != null){
                    loadRecipeDifficulty();
                }
                setUpChipGroup(selectedRecipe.getRecipeIngrdnts());
                binding.edtDescription.setText(selectedRecipe.getRecipeDescr());
            }
        }


    private void updateRecipe() {
        binding.editRecipeBtn.setVisibility(View.GONE);
        binding.editRecipeProgress.setVisibility(View.VISIBLE);
        //Retrieve updated recipe details from UI elements
        updatedRecipeName = binding.newRecipeName.getText().toString().trim();
        updatedRecipeType = binding.edtRecipeType.getText().toString().toString();
        updatedRecipeTime = binding.newCookingTime.getText().toString().trim();
        updatedRecipeDifficulty = getSelectedDifficulty();
        updatedRecipeDescription = binding.edtDescription.getText().toString().trim();


        //update recipe in fireStore
       if (selectedRecipe != null){
           DocumentReference documentReference = fireStore.collection("Recipes").document(selectedRecipe.getRecipeDocID());
           Map<String, Object> updated = new HashMap<>();
           updated.put("recipeName", updatedRecipeName);
           updated.put("recipeType", updatedRecipeType);
           updated.put("recipeTime", updatedRecipeTime);
           updated.put("recipeDiff", updatedRecipeDifficulty);
           updated.put("recipeIngrdnts", selectedRecipe.getRecipeIngrdnts());
           updated.put("recipeDescr", updatedRecipeDescription);
           documentReference.update(updated).addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                   if (task.isSuccessful()) {
                       binding.editRecipeBtn.setVisibility(View.VISIBLE);
                       binding.editRecipeProgress.setVisibility(View.GONE);
                       Toast.makeText(EditRecipe.this, "Recipe Updated Successfully", Toast.LENGTH_SHORT).show();
                   } else {
                       binding.editRecipeBtn.setVisibility(View.VISIBLE);
                       binding.editRecipeProgress.setVisibility(View.GONE);
                       Toast.makeText(EditRecipe.this, "Error Updating Recipe", Toast.LENGTH_SHORT).show();
                   }
               }
           });
       }
    }

    private String getSelectedDifficulty() {
        updatedSelectedRadioBtnID = binding.newDifficulty.getCheckedRadioButtonId();
        if (updatedSelectedRadioBtnID != -1) {
            // Get selected radio button
            RadioButton updatedSelectedRadioBtn = findViewById(updatedSelectedRadioBtnID);
            // Get text of a selected radio button
            return updatedSelectedRadioBtn.getText().toString().trim();
        }
        return "No Difficulty Selected";
    }
}