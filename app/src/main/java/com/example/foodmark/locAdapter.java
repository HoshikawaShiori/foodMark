package com.example.foodmark;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class locAdapter extends RecyclerView.Adapter<ViewHolder> {

    Context context;
    List<Item> items;

    public locAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = viewHolder.getAdapterPosition();
                new AlertDialog.Builder(context)
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete " + items.get(position).getTitle()+ "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String itemId = items.get(position).getId();

                                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("FoodMarks").child(itemId);
                                itemRef.removeValue();

                                items.remove(position);
                                notifyItemRemoved(position);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String base64Image = items.get(position).getLocationImage();
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        holder.imgView.setImageBitmap(decodedByte);
        holder.name.setText(items.get(position).getTitle());
        holder.coordinates.setText(items.get(position).getLongitude() + ", " + items.get(position).getLatitude());
        holder.loc.setText(items.get(position).getLocation());
        holder.desc.setText(items.get(position).getDescription());

        // Set an OnClickListener to the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the showFoodMark activity
                Intent intent = new Intent(context, showFoodMark.class);

                // Pass the id of the clicked item to the showFoodMark activity
                intent.putExtra("itemId", items.get(position).getId());

                // Start the showFoodMark activity
                context.startActivity(intent);
            }
        });
    }

    @Override

    public int getItemCount() {
        Log.d("locAdapter", "getItemCount: " + items.size());
        return items.size();
    }

    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
}
