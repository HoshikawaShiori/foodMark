package com.example.foodmark

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder.Companion.from
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.Style.Companion.STANDARD
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.*


class addFoodMark: AppCompatActivity() {
    private var sheet: FrameLayout? = null
    private var mapView: MapView? = null
    private var floatingActionButton: FloatingActionButton? = null
    private var addTitle: EditText? = null
    private var addLocation:EditText? = null
    private var addDescription:EditText? = null
    private var mDatabase: DatabaseReference? = null

    private val activityResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            }
        }

    private val onIndicatorBearingChangedListener =
        OnIndicatorBearingChangedListener { v ->
            mapView!!.mapboxMap.setCamera(
                CameraOptions.Builder().bearing(v).build()
            )
        }

    private val onIndicatorPositionChangedListener =
        OnIndicatorPositionChangedListener { point ->
            mapView!!.mapboxMap.setCamera(CameraOptions.Builder().center(point).zoom(18.0).build())
            mapView!!.gestures.focalPoint = mapView!!.mapboxMap.pixelForCoordinate(point)
        }

    private val onMapClickListener: OnMapClickListener = object : OnMapClickListener {
        override fun onMapClick(point: Point): Boolean {
            addAnnotationToMap(point)
            return true
        }
    }

    private val onMoveListener: OnMoveListener = object : OnMoveListener {
        override fun onMoveBegin(moveGestureDetector: MoveGestureDetector) {
            mapView!!.location.removeOnIndicatorBearingChangedListener(
                onIndicatorBearingChangedListener
            )
            mapView!!.location.removeOnIndicatorPositionChangedListener(
                onIndicatorPositionChangedListener
            )
            mapView!!.gestures.removeOnMoveListener(this)
            floatingActionButton!!.show()
        }

        override fun onMove(moveGestureDetector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(moveGestureDetector: MoveGestureDetector) {
        }
    }



    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_mark)
        sheet = findViewById<FrameLayout>(R.id.sheet)
        val behavior = BottomSheetBehavior.from<View>(
            sheet!!
        )
        behavior.peekHeight = 200
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        mapView = findViewById<MapView>(R.id.mapView)
        floatingActionButton = findViewById<FloatingActionButton>(R.id.focusLocation)
        addTitle = findViewById<EditText>(R.id.addTitle)
        addLocation = findViewById<EditText>(R.id.addLocation)
        addDescription = findViewById<EditText>(R.id.addDescription)
        val addButton: Button = findViewById<Button>(R.id.button)


        floatingActionButton!!.hide()

        mapView!!.mapboxMap.loadStyleUri(STANDARD, object : Style.OnStyleLoaded {
            override fun onStyleLoaded(style: Style) {
                val puck2d = LocationPuck2D()
                val marker = BitmapFactory.decodeResource(resources, R.drawable.baseline_location_on_24)

                mapView!!.mapboxMap.setCamera(CameraOptions.Builder().zoom(18.0).build())
                val locationComponentPlugin = mapView!!.location
                locationComponentPlugin.enabled = true

                puck2d.bearingImage = from(R.drawable.puck2d)
                style.addImage("marker", marker)
                locationComponentPlugin.locationPuck = puck2d
                locationComponentPlugin.puckBearingEnabled = true
                locationComponentPlugin.pulsingEnabled = true
                locationComponentPlugin.addOnIndicatorBearingChangedListener(
                    onIndicatorBearingChangedListener
                )
                locationComponentPlugin.addOnIndicatorPositionChangedListener(
                    onIndicatorPositionChangedListener
                )
                mapView!!.gestures.addOnMoveListener(onMoveListener)
                mapView!!.gestures.addOnMapClickListener(onMapClickListener)

                floatingActionButton!!.setOnClickListener {
                    locationComponentPlugin.addOnIndicatorBearingChangedListener(
                        onIndicatorBearingChangedListener
                    )
                    locationComponentPlugin.addOnIndicatorPositionChangedListener(
                        onIndicatorPositionChangedListener
                    )
                    mapView!!.gestures.addOnMoveListener(onMoveListener)
                    floatingActionButton!!.hide()
                }
            }
        })

        mDatabase = FirebaseDatabase.getInstance().getReference("items")

        addButton.setOnClickListener(View.OnClickListener {
            val title = addTitle!!.text.toString().trim { it <= ' ' }
            val location = addLocation!!.text.toString().trim { it <= ' ' }
            val description = addDescription!!.text.toString().trim { it <= ' ' }

            if (title.isEmpty() || location.isEmpty() || description.isEmpty()) {
                Toast.makeText(this@addFoodMark, "Please fill in all fieldsss", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            val item = Item(title, location, description)

            val itemId = mDatabase!!.push().key // Generate unique key for item
            if (itemId != null) {
                mDatabase!!.child(itemId).setValue(item)
                Toast.makeText(this@addFoodMark, "Foodmark added successfully", Toast.LENGTH_SHORT)
                    .show()
                addTitle!!.text.clear()
                addLocation!!.text.clear()
                addDescription!!.text.clear()
            } else {
                Toast.makeText(this@addFoodMark, "Failed to add Foodmark", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addAnnotationToMap( point: Point) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        val annotationApi = mapView?.annotations
        val annotationConfig = AnnotationConfig(null, null, null);

        val pointAnnotationManager = annotationApi?.createPointAnnotationManager(annotationConfig)

        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(point.longitude(), point.latitude() ))
            .withIconImage("marker")

        pointAnnotationManager?.create(pointAnnotationOptions)
    }

}