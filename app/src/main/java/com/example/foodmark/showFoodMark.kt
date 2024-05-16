package com.example.foodmark

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder.Companion.from
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class showFoodMark : AppCompatActivity() {

    private var sheet: FrameLayout? = null
    private var mapView: MapView? = null
    private var image: ImageView? = null
    private var title: TextView? = null
    private var description: TextView? = null
    private var coordinates: TextView? = null
    private var address: TextView? = null
    private var floatingActionButton: FloatingActionButton? = null
    private var editButton: ImageButton? = null
    private var annotationApi: AnnotationPlugin? = null
    private var annotationConfig: AnnotationConfig? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var annotations = mutableListOf<PointAnnotationOptions>()



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

            Log.d("onMoveListener", PointAnnotationOptions().toString())
        }

        override fun onMove(moveGestureDetector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(moveGestureDetector: MoveGestureDetector) {
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_food_mark)
        val itemId = intent.getStringExtra("itemId");



        sheet = findViewById<FrameLayout>(R.id.sheet)
        title = findViewById<TextView>(R.id.txtTitle)
        image = findViewById<ImageView>(R.id.markImage)
        coordinates = findViewById<TextView>(R.id.txtCoordinates)
        address = findViewById<TextView>(R.id.txtAddress)
        description = findViewById<TextView>(R.id.txtDesc)
        floatingActionButton = findViewById<FloatingActionButton>(R.id.ShowfocusLocation)
        mapView = findViewById<MapView>(R.id.ShowmapView)
        editButton = findViewById<ImageButton>(R.id.btnEdit)

        val mDatabase = FirebaseDatabase.getInstance().getReference("FoodMarks")

        mDatabase.child(itemId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // Get the Item object
                val item = dataSnapshot.getValue(Item::class.java)
                val base64Image = item?.locationImage
                val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                // Set the text of the TextViews
                title?.text = item?.title
                coordinates?.text = "${item?.latitude}, ${item?.longitude}"
                address?.text = item?.location
                description?.text = item?.description
                image?.setImageBitmap(decodedByte)
                val point = Point.fromLngLat(item?.longitude?.toDouble()!!, item?.latitude?.toDouble()!!)


                mapView!!.mapboxMap.loadStyleUri(Style.STANDARD, object : Style.OnStyleLoaded {
                    override fun onStyleLoaded(style: Style) {
                        val puck2d = LocationPuck2D()
                        val marker = drawableToBitmap(R.drawable.baseline_location_on_24)

                        mapView!!.mapboxMap.setCamera(CameraOptions.Builder().center(point).zoom(18.0).build())
                        val locationComponentPlugin = mapView!!.location
                        locationComponentPlugin.enabled = true


                        puck2d.bearingImage = from(R.drawable.puck2d)
                        style.addImage("marker", marker)
                        locationComponentPlugin.locationPuck = puck2d
                        locationComponentPlugin.puckBearingEnabled = true
                        locationComponentPlugin.pulsingEnabled = true

                        mapView!!.gestures.addOnMoveListener(onMoveListener)

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
                annotationApi = mapView?.annotations
                annotationConfig = AnnotationConfig(null, null, null)
                pointAnnotationManager = annotationApi?.createPointAnnotationManager(annotationConfig)

                createAnnotation(point)

                editButton?.setOnClickListener {
                    val intent = intent
                    intent.setClass(this@showFoodMark, editFoodMark::class.java)
                    intent.putExtra("itemId", itemId)
                    startActivity(intent)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })





        val behavior = BottomSheetBehavior.from<View>(
            sheet!!
        )
        behavior.peekHeight = 200
        behavior.state = BottomSheetBehavior.STATE_EXPANDED





        floatingActionButton!!.hide()

    }

    override fun onResume() {
        super.onResume()

        val itemId = intent.getStringExtra("itemId")
        val mDatabase = FirebaseDatabase.getInstance().getReference("FoodMarks")

        mDatabase.child(itemId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the Item object
                val item = dataSnapshot.getValue(Item::class.java)

                val base64Image = item?.locationImage
                val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                // Set the text of the TextViews
                title?.text = item?.title
                coordinates?.text = "${item?.latitude}, ${item?.longitude}"
                address?.text = item?.location
                description?.text = item?.description
                image?.setImageBitmap(decodedByte)

                // Refresh the map
                val point = Point.fromLngLat(item?.longitude?.toDouble()!!, item?.latitude?.toDouble()!!)
                mapView!!.mapboxMap.setCamera(CameraOptions.Builder().center(point).zoom(18.0).build())
                createAnnotation(point)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
    private fun drawableToBitmap(drawableId: Int): Bitmap {
        val drawable: Drawable = ContextCompat.getDrawable(this, drawableId)!!
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun createAnnotation(point: Point){


        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
            .withIconImage("marker")
            .withIconSize(1.3)

        if (annotations.size == 0) {
            annotations.add(pointAnnotationOptions)
        } else {
            pointAnnotationManager?.deleteAll()
            annotations[0] = pointAnnotationOptions
        }
        pointAnnotationManager?.create(annotations)

    }
}