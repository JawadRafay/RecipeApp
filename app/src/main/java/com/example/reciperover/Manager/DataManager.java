package com.example.reciperover.Manager;

import com.example.reciperover.Models.RecipeDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private Map<String, List<RecipeDetail>> userFavoritesMap;
    public DataManager() {
        userFavoritesMap = new HashMap<>();
    }
    public void addToFavorites(RecipeDetail recipeDetail){
        String userID = recipeDetail.getCurrentUserID();
        if (!userFavoritesMap.containsKey(userID)){
            userFavoritesMap.put(userID, new ArrayList<>());
        }
        List<RecipeDetail> userFavorites = userFavoritesMap.get(userID);
        userFavorites.add(recipeDetail);
    }

    public List<RecipeDetail> getFavoritesForUser(String userID){
        if (userFavoritesMap.containsKey(userID)){
            return userFavoritesMap.get(userID);
        }else {
            return new ArrayList<>();
        }
    }
}
