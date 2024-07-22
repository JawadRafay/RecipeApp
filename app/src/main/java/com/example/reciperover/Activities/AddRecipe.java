package com.example.reciperover.Activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.reciperover.Adapters.CustomItemAdp;
import com.example.reciperover.Models.CustomItem;
import com.example.reciperover.Models.RecipeDetail;
import com.example.reciperover.Models.User;
import com.example.reciperover.R;
import com.example.reciperover.databinding.ActivityAddRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AddRecipe extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ActivityAddRecipeBinding binding;
    ArrayList<CustomItem> customList;
    FirebaseFirestore fireStore;
    FirebaseAuth fAuth;
    private Uri imageUri;
    private FirebaseStorage storage;
    RecipeDetail rD;
    private StorageReference mStorageRef;
    int selectedRadioButtonId;
    User u;
    String name, type, time, diff, descr, imageUrl, userID,
            docID;
    private ArrayList<String> ingredientsList;
    List<String> ingrdnts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        rD = new RecipeDetail();
        fAuth = FirebaseAuth.getInstance();
        u = new User();
        userID = fAuth.getCurrentUser().getUid();
        u.setuId(userID);
        //u.setuId(rD.getCurrentUserID());
        fireStore = FirebaseFirestore.getInstance();
        binding.bacBtn.setOnClickListener(v -> {onBackPressed();});
        customList = getCustomList();
        CustomItemAdp customItemAdp = new CustomItemAdp(this,customList);
        binding.recipeType.setAdapter(customItemAdp);
        binding.recipeType.setOnItemSelectedListener(this);
        binding.addRecipeImg.setOnClickListener(v -> {openGallery();});
        binding.addRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeDetail rD = recipeData();
                if (isValid(rD)){
                    uploadPic();
                }
            }
        });
        //chips ======================================================================
        ingredientsList = new ArrayList<>();
        binding.addIngredientsBtn.setOnClickListener(v -> {
                String chipText = binding.addIngredients.getText().toString().trim();
                if (chipText.isEmpty()){
                    Toast.makeText(this, "Enter Ingredients", Toast.LENGTH_SHORT).show();
                }
                addChip(chipText);
            binding.addIngredients.getText().clear();
        });

    }
    private void addChip(String chipText) {
        if (!chipText.isEmpty() && !ingredientsList.contains(chipText)) {
            ingredientsList.add(chipText);
            Chip chip = new Chip(this);
            chip.setText(chipText);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeChip(chip, chipText);
                }
            });

            binding.chipGroupIngredients.addView(chip);
        }
    }
    private void removeChip(Chip chip, String chipText) {
        ingredientsList.remove(chipText);
        binding.chipGroupIngredients.removeView(chip);
    }
private ArrayList<CustomItem> getCustomList(){
        customList = new ArrayList<>();
        customList.add(new CustomItem("Choose Type",R.drawable.recipe_type));
        customList.add(new CustomItem("Pakistani",R.drawable.pakistan));
        customList.add(new CustomItem("Italian",R.drawable.italy));
        customList.add(new CustomItem("Indian",R.drawable.india));
        return customList;
}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        CustomItem item = (CustomItem)parent.getSelectedItem();
        if (position != 0) {
            Toast.makeText(this, item.getSpinnerItemName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private void openGallery(){
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
            Glide.with(this).load(imageUri).into(binding.addRecipeImg);
        }
    }
    RecipeDetail recipeData()
    {
        name = binding.recipeName.getText().toString().trim();
        // Get the selected item from the Spinner
        CustomItem selectedCustomItem = (CustomItem) binding.recipeType.getSelectedItem();
        // Get the text representation of the selected item
        type = selectedCustomItem != null ? selectedCustomItem.getSpinnerItemName() : "Don't Save Recipe Type";
        time = binding.cookingTime.getText().toString().trim();
        diff = getSelectedDifficulty();
        descr = binding.description.getText().toString().trim();
        //userID = rD.getCurrentUserID();
        ingrdnts = ingredientsList;
        rD.setRecipeName(name);
        rD.setRecipeType(type);
        rD.setRecipeTime(time);
        rD.setRecipeDiff(diff);
        rD.setRecipeDescr(descr);
        //rD.setCurrentUserID(userID);
        rD.setRecipeIngrdnts(ingrdnts);
        return rD;

    }
    private String getSelectedDifficulty() {
        // Get the selected radio button from the RadioGroup
        selectedRadioButtonId = binding.difficulty.getCheckedRadioButtonId();

        // Check if any radio button is selected
        if (selectedRadioButtonId != -1) {
            // Get the selected radio button
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);

            // Get the text of the selected radio button
            return selectedRadioButton.getText().toString();
        } else {
            // Handle the case where no radio button is selected
            return "No Difficulty Selected";
        }
    }
    private boolean isValid(RecipeDetail rD){
        CustomItem selectedCustomItem = (CustomItem) binding.recipeType.getSelectedItem();
        if (imageUri == null){
            Toast.makeText(this, "Select Recipe Image", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rD.getRecipeName().length() == 0){
            binding.recipeName.setError("Enter Correct Recipe Name");
            return false;
        }
        else if (selectedCustomItem != null && "Choose Type".equals(selectedCustomItem.getSpinnerItemName())) {
            Toast.makeText(this, "Select Food Type", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (rD.getRecipeTime() == null){
            binding.cookingTime.setError("Enter Cooking Time");
            return false;
        }
        else if (selectedRadioButtonId == -1){
            Toast.makeText(this, "Select Difficulty", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (rD.getRecipeDescr().length() < 20){
            binding.description.setError("Provide a detailed description!");
            return false;
        }
        else if (rD.getRecipeIngrdnts().isEmpty()){
            binding.addIngredients.setError("Provide a ingredients that used in this Recipe!");
            return false;
        }

        return true;
    }

    private boolean uploadPic(){
        binding.addRecipeProgress.setVisibility(View.VISIBLE);
        binding.addRecipeBtn.setVisibility(View.INVISIBLE);
        storage = FirebaseStorage.getInstance();
        if (imageUri != null){
            // put image into firebase Storage
            mStorageRef = storage.getReference().child("recipe_image/" + UUID.randomUUID() + ".jpg");
            mStorageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Downloading the url of image
                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            binding.addRecipeProgress.setVisibility(View.GONE);
                            binding.addRecipeBtn.setVisibility(View.VISIBLE);
                            //converting url into string
                            imageUrl = uri.toString();
                            addToDb(imageUrl);
                        }
                    });
                }
            });
        }
        return false;
    }
    private void addToDb(String imageUrl){
        //providing document ID
        docID = fireStore.collection("Recipes").document().getId();
        DocumentReference documentReference = fireStore.collection("Recipes").document(docID);
        //Storing recipe detail in rD
        RecipeDetail rD = new RecipeDetail(imageUrl,name,type,time,diff,descr, ingrdnts,userID,docID);
        documentReference.set(rD, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(AddRecipe.this, "Recipe saved Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddRecipe.this, ShowRecipeDetail.class);
                        intent.putStringArrayListExtra("INGREDIENTS_LIST", ingredientsList);
                        startActivity(intent);
                        finish();
                    }else
                    {
                        Toast.makeText(AddRecipe.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddRecipe.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}