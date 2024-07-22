package com.example.reciperover.Adapters;

import static com.example.reciperover.R.color.white;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reciperover.Activities.EditRecipe;
import com.example.reciperover.Models.RecipeDetail;
import com.example.reciperover.R;
import com.example.reciperover.SQLite.FavDB;
import com.example.reciperover.databinding.RecipeItemBinding;

import java.util.ArrayList;
import java.util.List;

public class ShowRecipeAdp extends RecyclerView.Adapter<ShowRecipeAdp.ShowRecipeHolder> {
    private List<RecipeDetail> rD;
    private List<RecipeDetail> originalData;
    private boolean isInHomeFragment;
    private boolean isInFavoritesFragment;
    private ItemClickListener itemClickListener;
    private Context mContext;

    public ShowRecipeAdp(Context context, List<RecipeDetail> rD, ItemClickListener itemClickListener, boolean isInHomeFragment,boolean isInFavoritesFragment) {
        this.mContext = context;
        this.rD = rD;
        this.itemClickListener = itemClickListener;
        this.originalData = new ArrayList<>(rD);
        this.isInHomeFragment = isInHomeFragment;
        this.isInFavoritesFragment = isInFavoritesFragment;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRecipesDetail(List<RecipeDetail> rD) {
        this.rD = rD;
        this.originalData = new ArrayList<>(rD);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShowRecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecipeItemBinding binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new ShowRecipeHolder(binding);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ShowRecipeHolder holder, final int position) {
        RecipeDetail currentRecipe = rD.get(position);
        holder.bindData(currentRecipe);

        holder.itemView.setOnClickListener(v -> {
            itemClickListener.onItemClick(rD.get(position)); // this will get the position of our item in recycler......
        });
        holder.binding.deleteRecipe.setOnClickListener(v -> {
            itemClickListener.onDeleteClick(currentRecipe, position);
        });
        holder.binding.editRecipe.setOnClickListener(v -> {
            itemClickListener.onEditClick(currentRecipe);
        });
        if (isInHomeFragment) {
            holder.binding.editRecipe.setVisibility(View.GONE);
            holder.binding.deleteRecipe.setVisibility(View.GONE);
        } else {
            holder.binding.editRecipe.setVisibility(View.VISIBLE);
            holder.binding.deleteRecipe.setVisibility(View.VISIBLE);
            holder.binding.bookMark.setVisibility(View.GONE);
        }
        if (isInFavoritesFragment){
            holder.binding.editRecipe.setVisibility(View.GONE);
            holder.binding.deleteRecipe.setVisibility(View.VISIBLE);
            holder.binding.bookMark.setVisibility(View.GONE);
        }
        holder.binding.bookMark.setOnClickListener(v -> {
            itemClickListener.onAddToFavoritesClick(currentRecipe, position);
            Toast.makeText(mContext, "Recipe Saved", Toast.LENGTH_SHORT).show();
            saveToFavorite(currentRecipe);
        });
    }

    private void saveToFavorite(RecipeDetail recipe) {
        FavDB favDB = new FavDB(mContext);
        favDB.insertIntoDataBase(
                recipe.getCurrentUserID(),
                recipe.getRecipeDocID(),
                recipe.getRecipeName(),
                recipe.getRecipeType(),
                recipe.getRecipeTime(),
                recipe.getRecipeDiff(),
                TextUtils.join(",", recipe.getRecipeIngrdnts()),
                recipe.getRecipeDescr(),
                recipe.getRecipeImageUrl(),
                "1" // 1 for favorite status

        );
    }

    public void filter(String query) {

        List<RecipeDetail> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(rD);
            notifyDataSetChanged();
        } else {
            for (RecipeDetail item : originalData) {
                if (item.getRecipeName().toLowerCase().contains(query.toLowerCase()) ||
                        item.getRecipeType().toLowerCase().contains(query.toLowerCase()) ||
                item.getRecipeTime().toLowerCase().contains(query.toLowerCase())
                || containsIngredient(item.getRecipeIngrdnts(),query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        //  Log.d("TAG","Error getting"+filteredList);
        Log.d("TAG", "data" + query);
        setRecipesDetail(filteredList);
    }
    private boolean containsIngredient(List<String> ingredients, String query) {
        String[] queryTerms = query.split("\\s+");
        for (String term : queryTerms) {
            boolean termFound = false;
            for (String ingredient : ingredients) {
                if (ingredient.toLowerCase().contains(term.toLowerCase())) {
                    termFound = true;
                    break;
                }
            }
            if (!termFound) {
                return false;
            }
        }
        return true;
    }

    public void removeItem(int position) {
        rD.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return rD.size();
    }

    public interface ItemClickListener {
        void onItemClick(RecipeDetail recipeDetail);

        void onDeleteClick(RecipeDetail recipeDetail, int position);

        void onEditClick(RecipeDetail recipeDetail);

        void onAddToFavoritesClick(RecipeDetail recipeDetail, int position);
    }

    public static class ShowRecipeHolder extends RecyclerView.ViewHolder {
        private final RecipeItemBinding binding;

        public ShowRecipeHolder(@NonNull RecipeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(RecipeDetail rD) {
            binding.recipeName.setText(rD.getRecipeName());
            binding.chefName.setText(rD.getRecipeType());
            binding.timeTaken.setText(rD.getRecipeTime());

            Glide.with(binding.userRecipeImage.getContext())
                    .load(rD.getRecipeImageUrl())
                    .into(binding.userRecipeImage);
        }

    }

}