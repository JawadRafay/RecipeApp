package com.example.reciperover.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.reciperover.Models.RecipeDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorite_recipes";
    private static final int DATABASE_VERSION = 1;
    // Define table and columns name
    private static final String TABLE_SAVED_RECIPES = "saved_recipes";
    private static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_RECIPE_DOC_ID = "recipe_doc_id";
    public static final String COLUMN_RECIPE_NAME = "recipe_name";
    public static final String COLUMN_RECIPE_TYPE = "recipe_type";
    public static final String COLUMN_RECIPE_TIME = "recipe_time";
    public static final String COLUMN_RECIPE_DIFFICULTY = "recipe_difficulty";
    public static final String COLUMN_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COLUMN_RECIPE_DESCRIPTION = "recipe_description";
    public static final String COLUMN_RECIPE_IMAGE_URL = "recipe_image_url";

    private static final String COLUMN_FAVORITE_STATUS  = "fStatus";

    // Database creation SQL statement
    private static final String DATABASE_CREATE =
            "create table " + TABLE_SAVED_RECIPES + "(" +
                    COLUMN_RECIPE_DOC_ID + " text primary key, " +
                    COLUMN_USER_ID + " text not null, "+
                    COLUMN_RECIPE_NAME + " text not null, " +
                    COLUMN_RECIPE_TYPE + " text, " +
                    COLUMN_RECIPE_TIME + " text, " +
                    COLUMN_RECIPE_DIFFICULTY + " text, " +
                    COLUMN_RECIPE_INGREDIENTS + " text, " +
                    COLUMN_RECIPE_DESCRIPTION + " text, " +
                    COLUMN_RECIPE_IMAGE_URL + " text, " + COLUMN_FAVORITE_STATUS + " text)";


    public FavDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //insert data into database
    public void insertIntoDataBase(String user_id,String recipe_doc_id, String recipe_name, String recipe_type, String recipe_time,
                                   String recipe_difficulty, String recipe_ingredients,
                                   String recipe_description, String recipe_image_url, String fav_status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RECIPE_DOC_ID, recipe_doc_id);
        cv.put(COLUMN_USER_ID,user_id);
        cv.put(COLUMN_RECIPE_NAME, recipe_name);
        cv.put(COLUMN_RECIPE_TYPE, recipe_type);
        cv.put(COLUMN_RECIPE_TIME, recipe_time);
        cv.put(COLUMN_RECIPE_DIFFICULTY, recipe_difficulty);
        cv.put(COLUMN_RECIPE_INGREDIENTS, recipe_ingredients);
        cv.put(COLUMN_RECIPE_DESCRIPTION, recipe_description);
        cv.put(COLUMN_RECIPE_IMAGE_URL, recipe_image_url);
        cv.put(COLUMN_FAVORITE_STATUS, fav_status);
        db.insert(TABLE_SAVED_RECIPES, null, cv);
        db.close();
    }

    //read data
    public Cursor readAllData(String user_id,String recipe_doc_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_SAVED_RECIPES + " where " + COLUMN_USER_ID + "='" + user_id + "' and " + COLUMN_RECIPE_DOC_ID + "='" + recipe_doc_id + "'";
        return db.rawQuery(sql, null, null);

    }

    public void removeFav(String recipe_doc_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_SAVED_RECIPES + " SET " + COLUMN_FAVORITE_STATUS + " ='0' WHERE " + COLUMN_RECIPE_DOC_ID +"="+
                recipe_doc_id + "";
        db.execSQL(sql);
    }

    //select all favorite list
    public Cursor selectAllFavoriteList(String user_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_SAVED_RECIPES + " WHERE " + COLUMN_USER_ID + "='" + user_id + "' AND " + COLUMN_FAVORITE_STATUS + "='1'";
        return db.rawQuery(sql,null,null);
    }
    public void deleteFavoriteRecipe(String recipeDocID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SAVED_RECIPES, COLUMN_RECIPE_DOC_ID + " = ?", new String[]{recipeDocID});
        db.close();
    }
}
