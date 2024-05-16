package com.example.foodmark;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private List<Item> items = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView catRecyclerView;
    private TextView counter;
    private locAdapter adapter;
    private catAdapter catadapter;
    private Button btnAdd;
    private ChipGroup chipGroup;
    private EditText search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ChipGroup chipGroup = findViewById(R.id.catGroup);
        search = findViewById(R.id.txtSearch);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mDatabase = FirebaseDatabase.getInstance().getReference("FoodMarks");
        recyclerView = findViewById(R.id.recyclerItems);
        counter = findViewById(R.id.txtCount);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new locAdapter(this, items);
        recyclerView.setAdapter(adapter);

        btnAdd= findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("Food Mark", "Button clicked!");
                Intent activityChangeIntent = new Intent(MainActivity.this, addFoodMark.class);
                MainActivity.this.startActivity(activityChangeIntent);

            }

        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed here
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Get the search query
                String query = s.toString().toLowerCase();

                if (!query.isEmpty()) {
                    // Filter the items based on the query
                    List<Item> filteredItems = new ArrayList<>();
                    for (Item item : items) {
                        if (item.getTitle().toLowerCase().contains(query)) {
                            filteredItems.add(item);
                        }
                    }

                    // Update the adapter with the filtered items
                    adapter.updateItems(filteredItems);
                } else {
                    // If the query is empty, display all items
                    adapter.updateItems(items);
                }
            }
        });
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                // Get the checked chip
                Chip checkedChip = group.findViewById(checkedId);

                if (checkedChip != null) {
                    String category = checkedChip.getText().toString();

                    // Filter the items based on the category
                    List<Item> filteredItems = new ArrayList<>();
                    for (Item item : items) {
                        if (item.getCategory().equals(category)) {
                            filteredItems.add(item);
                        }
                    }

                    // Update the adapter with the filtered items
                    adapter.updateItems(filteredItems);
                } else {
                    // If no chip is checked, display all items
                    adapter.updateItems(items);
                }
            }
        });
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chipGroup.removeAllViews();
                items.clear();
                Set<String> categories = new HashSet<>(); // Create a set to store the categories
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item item = snapshot.getValue(Item.class);
                    items.add(item);

                    String category = item.getCategory();
                    if (!categories.contains(category)) { // Check if the category already exists in the set
                        categories.add(category); // Add the category to the set

                        // Create the chip
                        Chip chip = new Chip(MainActivity.this);
                        chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
                        chip.setClickable(true);
                        chip.setCheckable(true);
                        chip.setTypeface(ResourcesCompat.getFont(MainActivity.this, R.font.poppins));
                        chip.setText(category);
                        chip.setTextColor(getResources().getColor(R.color.white));
                        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#6C60DF")));
                        chip.setChipStrokeWidth(1f);
                        chip.setRippleColor(ColorStateList.valueOf(Color.parseColor("#6C60DF")));
                        chip.setRippleColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_300)));

                        chipGroup.addView(chip);
                    }
                }
                counter.setText(Integer.toString(items.size()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });




    }
}