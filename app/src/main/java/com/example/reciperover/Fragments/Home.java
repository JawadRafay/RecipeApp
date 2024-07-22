package com.example.reciperover.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.reciperover.Activities.ShowRecipeDetail;
import com.example.reciperover.Adapters.ShowRecipeAdp;
import com.example.reciperover.Models.RecipeDetail;
import com.example.reciperover.Models.User;
import com.example.reciperover.R;
import com.example.reciperover.databinding.FragmentHomeBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Home extends Fragment {
    FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    DocumentReference documentReference;
    private ShowRecipeAdp adp;
    List<RecipeDetail> rL;
    private ArrayList<String> ingredientsList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        dataManager = new DataManager();
        ingredientsList = new ArrayList<>();
        if (currentUser != null) {
            //String userID = currentUser.getUid();
            String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            documentReference = db.collection("Users").document(userID);
            documentReference.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("profilePicUrl");
                    if (imageUrl != null) {
                        Glide.with(requireContext()).load(imageUrl).into(binding.userImg);
                    }
                }
            }).addOnFailureListener(e -> Log.d("TAG", "Error getting user data: " + e.getMessage()));
            // Fetch user data from FireStore using the retrieved user ID
            getUserDataFromFireStore();
        }
        adp = new ShowRecipeAdp(requireContext(), new ArrayList<>(), new ShowRecipeAdp.ItemClickListener() {
            @Override
            public void onItemClick(RecipeDetail recipeDetail) {
                toShowRecipeDetail(recipeDetail);
            }

            @Override
            public void onDeleteClick(RecipeDetail recipeDetail, int position) {
            }

            @Override
            public void onEditClick(RecipeDetail recipeDetail) {

            }

            @Override
            public void onAddToFavoritesClick(RecipeDetail recipeDetail, int position) {

            }

        }, true,false);
        loadRecipes();
        setUpSearchView();
        binding.filterIngredients.setOnClickListener(v -> showDialog());
        return view;
    }


    @SuppressLint("SetTextI18n")
    private void getUserDataFromFireStore() {
        documentReference = db.collection("Users").document(Objects.requireNonNull(mAuth.getUid()));
        documentReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("TAG", "Error getting document: " + (error.getMessage() != null ? error.getMessage() : "No error message available"));
                return;
            }

            if (value != null && value.exists()) {
                User user = value.toObject(User.class);
                if (user != null && user.getName() != null) {
                    binding.profileTxt.setText("Hello, " + user.getName());
                }
            } else {
                Log.e("TAG", "Document does not exist");
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadRecipes() {
        rL = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.showRecipeRecycler.setLayoutManager(layoutManager);
        binding.showRecipeRecycler.setAdapter(adp);
        CollectionReference cR = db.collection("Recipes");
        cR.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                RecipeDetail recipeDetail = document.toObject(RecipeDetail.class);
                rL.add(recipeDetail);
            }
            adp.setRecipesDetail(rL);
            adp.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void toShowRecipeDetail(RecipeDetail recipeDetail) {
        Intent intent = new Intent(getContext(), ShowRecipeDetail.class);
        intent.putExtra("docID", recipeDetail.getRecipeDocID());
        startActivity(intent);
    }

    private void setUpSearchView() {
        binding.searchRecipe.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.isEmpty()) {
                    adp.setRecipesDetail(rL);
                    adp.notifyDataSetChanged();
                } else adp.filter(newText);
                return true;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showDialog() {
        final Dialog dialog = new Dialog(Objects.requireNonNull(requireContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);
        EditText filterIngredientsEdtText = dialog.findViewById(R.id.filterIngredientsEdtText);
        ImageButton showFilterIngredientsBtn = dialog.findViewById(R.id.showFilterIngredientsBtn);
        ChipGroup filterChipGroupIngredients = dialog.findViewById(R.id.filterChipGroupIngredients);
        AppCompatButton filteredIngredients = dialog.findViewById(R.id.filteredIngredients);


        //setting action on bottom sheet widgets
        showFilterIngredientsBtn.setOnClickListener(v -> {
            String chipText = filterIngredientsEdtText.getText().toString().trim();
            if (chipText.isEmpty()) {
                Toast.makeText(getContext(), "Enter ingredients for filter", Toast.LENGTH_SHORT).show();
            } else {
                addChip(dialog, chipText);
                filterIngredientsEdtText.getText().clear();
            }
        });
        filteredIngredients.setOnClickListener(v -> {
            @SuppressLint("NotifyDataSetChanged")

            List<String> selectedIngredients = getSelectedIngredients(filterChipGroupIngredients);
            List<RecipeDetail> filteredRecipes = new ArrayList<>();

            for (RecipeDetail recipe : rL) {
                if (recipeContainsIngredients(recipe, selectedIngredients)) {
                    filteredRecipes.add(recipe);
                }
            }

            adp.setRecipesDetail(filteredRecipes);
            adp.notifyDataSetChanged();
            dialog.dismiss();

        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private Chip addChip(Dialog dialog, String chipText) {
        ChipGroup filterChipGroupIngredients = dialog.findViewById(R.id.filterChipGroupIngredients);
        Chip chip = new Chip(getContext());
        if (filterChipGroupIngredients != null && !chipText.isEmpty() && !ingredientsList.contains(chipText)) {
            ingredientsList.add(chipText);
            chip.setText(chipText);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> removeChip(filterChipGroupIngredients, chip));
            filterChipGroupIngredients.addView(chip);
        }
        return chip;
    }

    private void removeChip(ChipGroup chipGroup, Chip chip) {
        chipGroup.removeView(chip);
        ingredientsList.remove(chip.getText().toString().trim());
    }

    private List<String> getSelectedIngredients(ChipGroup chipGroup) {
        List<String> selectedIngredients = new ArrayList<>();

        // Iterate through the chips in the ChipGroup
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                selectedIngredients.add(chip.getText().toString().trim());
            }
        }

        return selectedIngredients;
    }

    private boolean recipeContainsIngredients(RecipeDetail recipe, List<String> selectedIngredients) {
        List<String> recipeIngredients = recipe.getRecipeIngrdnts();

        // Check if any selected ingredient is present in the recipe's ingredients
        return recipeIngredients != null && !Collections.disjoint(recipeIngredients, selectedIngredients);
    }

}