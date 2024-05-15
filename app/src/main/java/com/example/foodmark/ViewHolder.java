package com.example.foodmark;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    ImageView imgView;
    TextView name, loc, desc, coords;
    public ViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
        super(itemView);

        imgView= itemView.findViewById(R.id.image);
        name= itemView.findViewById(R.id.title);
        coords= itemView.findViewById(R.id.txtCoords);
        loc= itemView.findViewById(R.id.location);
        desc = itemView.findViewById(R.id.description);
    }
}
