package com.example.foodmark;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class catAdapter extends RecyclerView.Adapter<catViewHolder>  {

    Context context;
    List<Item> items;

    public catAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public catViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cat_view, parent, false);
        return new catViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull catViewHolder holder, int position) {
        holder.chip.setText(items.get(position).getCategory());
    }

    @Override
    public int getItemCount() {
        Log.d("catAdapter", "getItemCount: " + items.size());
        return items.size();

    }
}