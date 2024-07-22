package com.example.reciperover.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.reciperover.Activities.AddRecipe;
import com.example.reciperover.Models.CustomItem;
import com.example.reciperover.R;

import java.util.ArrayList;

public class CustomItemAdp extends ArrayAdapter {
    public CustomItemAdp(@NonNull AddRecipe context, ArrayList<CustomItem> customList) {
        super(context, 0, customList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_layout,parent,false);

        }
        CustomItem item = (CustomItem) getItem(position);
        ImageView spinnerIV = convertView.findViewById(R.id.ivSpinnerLayout);
        TextView spinnerTV = convertView.findViewById(R.id.chooseType);
        if (item != null) {
            spinnerIV.setImageResource(item.getSpinnerItemImage());
            spinnerTV.setText(item.getSpinnerItemName());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.custom_dropdown_layout,parent,false);
        }
        CustomItem item = (CustomItem) getItem(position);
        ImageView dropDownIV = convertView.findViewById(R.id.ivDropDown);
        TextView dropDownTV = convertView.findViewById(R.id.tvDropDown);
        if (item != null) {
            dropDownIV.setImageResource(item.getSpinnerItemImage());
            dropDownTV.setText(item.getSpinnerItemName());
        }
        return convertView;
    }
}
