package com.example.reciperover.Models;

import java.io.Serializable;
import java.util.List;

public class RecipeDetail implements Serializable {
    private String recipeImageUrl = "";
    private String recipeName;
    private String recipeType = "";
    private String recipeTime = "";
    private String recipeDiff = "";
    private String recipeDescr = "";
    private List<String> recipeIngrdnts;
    private String currentUserID = "";
    private String recipeDocID = "";

    public RecipeDetail() {
    }

    public RecipeDetail(String recipeImageUrl, String recipeName, String recipeType, String recipeTime, String recipeDiff, String recipeDescr,  List<String> recipeIngrdnts, String currentUserID, String recipeDocID) {
        this.recipeImageUrl = recipeImageUrl;
        this.recipeName = recipeName;
        this.recipeType = recipeType;
        this.recipeTime = recipeTime;
        this.recipeDiff = recipeDiff;
        this.recipeDescr = recipeDescr;
        this.recipeIngrdnts = recipeIngrdnts;
        this.currentUserID = currentUserID;
        this.recipeDocID = recipeDocID;
    }

    public String getRecipeImageUrl() {
        return recipeImageUrl;
    }

    public void setRecipeImageUrl(String recipeImageUrl) {
        this.recipeImageUrl = recipeImageUrl;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(String recipeType) {
        this.recipeType = recipeType;
    }

    public String getRecipeTime() {
        return recipeTime;
    }

    public void setRecipeTime(String recipeTime) {
        this.recipeTime = recipeTime;
    }

    public String getRecipeDiff() {
        return recipeDiff;
    }

    public void setRecipeDiff(String recipeDiff) {
        this.recipeDiff = recipeDiff;
    }

    public String getRecipeDescr() {
        return recipeDescr;
    }

    public void setRecipeDescr(String recipeDescr) {
        this.recipeDescr = recipeDescr;
    }

    public List<String> getRecipeIngrdnts() {
        return recipeIngrdnts;
    }

    public void setRecipeIngrdnts(List<String> recipeIngrdnts) {
        this.recipeIngrdnts = recipeIngrdnts;
    }

    public String getCurrentUserID() {
        return currentUserID;
    }

    public void setCurrentUserID(String currentUserID) {
        this.currentUserID = currentUserID;
    }

    public String getRecipeDocID() {
        return recipeDocID;
    }

    public void setRecipeDocID(String recipeDocID) {
        this.recipeDocID = recipeDocID;
    }
}
