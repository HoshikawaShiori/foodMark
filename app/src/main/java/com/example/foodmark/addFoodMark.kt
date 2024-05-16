package com.example.foodmark

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
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
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.*
import java.io.ByteArrayOutputStream


class addFoodMark: AppCompatActivity() {
    private var sheet: FrameLayout? = null
    private var mapView: MapView? = null
    private var floatingActionButton: FloatingActionButton? = null
    private var addTitle: EditText? = null
    private var Coords:TextView? = null
    private var addGallery:Button? = null
    private var addCamera:Button? = null
    private var addLocation:EditText? = null
    private var addDescription:TextInputEditText? = null
    private var addCategory:EditText? = null
    private var mDatabase: DatabaseReference? = null
    private var annotations = mutableListOf<PointAnnotationOptions>()
    private var annotationApi: AnnotationPlugin? = null
    private var annotationConfig: AnnotationConfig? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var locImage: Bitmap? = null
    private var coordinate: Point? = null

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
            createAnnotation(point)
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

            Log.d("onMoveListener", PointAnnotationOptions().toString())
        }

        override fun onMove(moveGestureDetector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(moveGestureDetector: MoveGestureDetector) {
        }
    }

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            locImage = imageBitmap;
        }
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            locImage = imageBitmap
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
        addDescription = findViewById<TextInputEditText>(R.id.addDescription)
        addGallery= findViewById<Button>(R.id.addGallery)
        addCamera= findViewById<Button>(R.id.addCamera)
        Coords = findViewById<TextView>(R.id.txtCoords)
        addCategory = findViewById<EditText>(R.id.addCategory)
        val addButton: Button = findViewById<Button>(R.id.button)


        floatingActionButton!!.hide()

        mapView!!.mapboxMap.loadStyleUri(STANDARD, object : Style.OnStyleLoaded {
            override fun onStyleLoaded(style: Style) {
                val puck2d = LocationPuck2D()
                val marker = drawableToBitmap(R.drawable.baseline_location_on_24)

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

        annotationApi = mapView?.annotations
        annotationConfig = AnnotationConfig(null, null, null)
        pointAnnotationManager = annotationApi?.createPointAnnotationManager(annotationConfig)
        mDatabase = FirebaseDatabase.getInstance().getReference("FoodMarks")

        addButton.setOnClickListener(View.OnClickListener {
            storeFoodMark()
        })

        addGallery = findViewById<Button>(R.id.addGallery)
        addCamera = findViewById<Button>(R.id.addCamera)

        addCamera?.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraResultLauncher.launch(cameraIntent)
        }
        addGallery?.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            galleryResultLauncher.launch(galleryIntent)
        }
    }

    private fun storeFoodMark() {
        val locationImage = bitmapToBase64(locImage!!)
        val title = addTitle!!.text.toString().trim { it <= ' ' }
        val location = addLocation!!.text.toString().trim { it <= ' ' }
        val long = coordinate?.longitude().toString()
        val lat = coordinate?.latitude().toString()
        val description = addDescription!!.text.toString().trim { it <= ' ' }
        val category = addCategory!!.text.toString().trim { it <= ' ' }

        // Check if all fields are filled
        if (title.isEmpty() || location.isEmpty() || coordinate == null || description.isEmpty() || category.isEmpty() || locImage == null) {
            Toast.makeText(this@addFoodMark, "All fields are mandatory", Toast.LENGTH_SHORT).show()
            return
        }


        val itemId = mDatabase!!.push().key // Generate unique key for item
        if (itemId != null) {
            val item:Item = Item(locationImage, title, location, lat, long, description, category, itemId)
            mDatabase!!.child(itemId).setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(this@addFoodMark, "Foodmark added successfully", Toast.LENGTH_SHORT).show()
                    addTitle!!.text.clear()
                    addLocation!!.text.clear()
                    addDescription!!.text!!.clear()
                    addCategory!!.text.clear()

                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@addFoodMark, "Failed to add Foodmark", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this@addFoodMark, "Failed to generate unique key for Foodmark", Toast.LENGTH_SHORT).show()
        }
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
        Coords?.text = "Latitude: ${point.latitude()} Longitude: ${point.longitude()}";
        coordinate = point;
    }

    private fun drawableToBitmap(drawableId: Int): Bitmap {
        val drawable: Drawable = ContextCompat.getDrawable(this, drawableId)!!
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

}