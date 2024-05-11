package com.example.foodmark;


import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.createDefault2DPuck;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.plugin.compass.CompassPlugin;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class addMark extends AppCompatActivity {

    private FrameLayout sheet;
    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private EditText addTitle, addLocation, addDescription;
    private DatabaseReference mDatabase;


    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(addMark.this, "Permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
        @Override
        public void onIndicatorBearingChanged(double v) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
        }
    };

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(20.0).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mark);
        sheet = findViewById(R.id.sheet);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
        behavior.setPeekHeight(200);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mapView = findViewById(R.id.mapView);
        floatingActionButton = findViewById(R.id.focusLocation);
        addTitle = findViewById(R.id.addTitle);
        addLocation = findViewById(R.id.addLocation);
        addDescription = findViewById(R.id.addDescription);
        Button addButton = findViewById(R.id.button);


        floatingActionButton.hide();
        mapView.getMapboxMap().loadStyleUri(Style.STANDARD, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                LocationPuck2D puck2d=new LocationPuck2D();
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
                locationComponentPlugin.setEnabled(true);
                puck2d.setBearingImage(ImageHolder.from(R.drawable.puck2d));
                locationComponentPlugin.setLocationPuck(puck2d);
                locationComponentPlugin.setPuckBearingEnabled(true);
                locationComponentPlugin.setPulsingEnabled(true);
                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);

                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        floatingActionButton.hide();
                    }
                });
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference("items");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = addTitle.getText().toString().trim();
                String location = addLocation.getText().toString().trim();
                String description = addDescription.getText().toString().trim();

                if (title.isEmpty() || location.isEmpty() || description.isEmpty()) {
                    Toast.makeText(addMark.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Item item = new Item(title, location, description);

                String itemId = mDatabase.push().getKey(); // Generate unique key for item
                if (itemId != null) {
                    mDatabase.child(itemId).setValue(item);
                    Toast.makeText(addMark.this, "Foodmark added successfully", Toast.LENGTH_SHORT).show();
                    addTitle.getText().clear();
                    addLocation.getText().clear();
                    addDescription.getText().clear();
                } else {
                    Toast.makeText(addMark.this, "Failed to add Foodmark", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}