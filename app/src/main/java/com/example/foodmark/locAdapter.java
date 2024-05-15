package com.example.foodmark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
        String base64Image = items.get(position).getLocationImage();
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        
        holder.imgView.setImageBitmap(decodedByte);
        holder.name.setText(items.get(position).getTitle());
        holder.coords.setText(items.get(position).getLatitude() + ", " + items.get(position).getLongitude());
        holder.loc.setText(items.get(position).getLocation());
        holder.desc.setText(items.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
