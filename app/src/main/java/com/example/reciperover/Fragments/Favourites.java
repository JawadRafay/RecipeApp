package com.example.reciperover.Fragments;

import static com.example.reciperover.Activities.Login.TAG;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_DESCRIPTION;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_DIFFICULTY;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_DOC_ID;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_IMAGE_URL;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_INGREDIENTS;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_NAME;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_TIME;
import static com.example.reciperover.SQLite.FavDB.COLUMN_RECIPE_TYPE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.reciperover.Activities.ShowRecipeDetail;
import com.example.reciperover.Adapters.ShowRecipeAdp;
import com.example.reciperover.Models.RecipeDetail;
import com.example.reciperover.R;
import com.example.reciperover.SQLite.FavDB;
import com.example.reciperover.databinding.FragmentFavouritesBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Favourites extends Fragment {
    FragmentFavouritesBinding binding;
    ShowRecipeAdp adp;
    FirebaseAuth firebaseAuth;
    RecipeDetail rd;
    FirebaseFirestore db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavouritesBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        rd = new RecipeDetail();
        db = FirebaseFirestore.getInstance();
        String userID = rd.getCurrentUserID();
        firebaseAuth =FirebaseAuth.getInstance();
        FavDB favDB = new FavDB(getContext());
        Cursor cursor = favDB.selectAllFavoriteList(firebaseAuth.getCurrentUser().getUid());
        List<RecipeDetail> savedRecipes = cursorToRecipeList(cursor);
        adp = new ShowRecipeAdp(requireContext(), savedRecipes, new ShowRecipeAdp.ItemClickListener() {
            @Override
            public void onItemClick(RecipeDetail recipeDetail) {
                toShowRecipeDetail(recipeDetail);
            }

            @Override
            public void onDeleteClick(RecipeDetail recipeDetail, int position) {
                FavDB dbHelper = new FavDB(getContext());  // Replace with your actual context
                dbHelper.deleteFavoriteRecipe(recipeDetail.getRecipeDocID());
                dbHelper.close();
                adp.removeItem(position);
                adp.notifyItemRemoved(position);
            }

            @Override
            public void onEditClick(RecipeDetail recipeDetail) {

            }

            @Override
            public void onAddToFavoritesClick(RecipeDetail recipeDetail, int position) {

            }
        },true,true);
        binding.favoriteRecipeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.favoriteRecipeRecycler.setAdapter(adp);
        return view;

    }
    private List<RecipeDetail> cursorToRecipeList(Cursor cursor) {
        List<RecipeDetail> recipeList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                 @SuppressLint("Range") String recipeDocID = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_DOC_ID));
                @SuppressLint("Range") String recipeName = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_NAME));
                @SuppressLint("Range") String recipeType = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_TYPE));
                @SuppressLint("Range") String recipeTime = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_TIME));
                @SuppressLint("Range") String recipeDifficulty = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_DIFFICULTY));
                @SuppressLint("Range") String recipeIngredients = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_INGREDIENTS));
                List<String> ingredientsList = Arrays.asList(TextUtils.split(recipeIngredients, ","));
                @SuppressLint("Range") String recipeDescription = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_DESCRIPTION));
                @SuppressLint("Range") String recipeImageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_IMAGE_URL));

                RecipeDetail recipe = new RecipeDetail(recipeImageUrl, recipeName, recipeType, recipeTime, recipeDifficulty, recipeDescription, ingredientsList, "", recipeDocID);
                recipeList.add(recipe);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return recipeList;
    }
    private void toShowRecipeDetail(RecipeDetail recipeDetail) {
        Intent intent = new Intent(getContext(), ShowRecipeDetail.class);
        intent.putExtra("docID", recipeDetail.getRecipeDocID());
        startActivity(intent);
    }


}
