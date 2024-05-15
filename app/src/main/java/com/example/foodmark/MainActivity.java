package com.example.foodmark;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private List<Item> items = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView counter;
    private locAdapter adapter;
    private Button btnAdd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item item = snapshot.getValue(Item.class);
                    items.add(item);
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