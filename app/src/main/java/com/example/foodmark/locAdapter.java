package com.example.foodmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class locAdapter extends RecyclerView.Adapter<ViewHolder> {

    Context context;

    public locAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    List<Item> items;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(items.get(position).getName());
        holder.loc.setText(items.get(position).getLocation());
        holder.desc.setText(items.get(position).getDescription());
        holder.imgView.setImageResource(items.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
