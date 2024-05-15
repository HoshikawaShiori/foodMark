package com.example.foodmark;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.google.android.material.chip.Chip;

public class catViewHolder extends RecyclerView.ViewHolder{

    Chip chip;

    public catViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
        super(itemView);
        chip = itemView.findViewById(R.id.chip_category);
    }
}