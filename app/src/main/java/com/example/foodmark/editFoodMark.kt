package com.example.foodmark

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.ImageHolder.Companion.from
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.io.ByteArrayOutputStream

class editFoodMark : AppCompatActivity() {

    private var sheet: FrameLayout? = null
    private var mapView: MapView? = null
    private var image: ImageView? = null
    private var title: EditText? = null
    private var coordinates: TextView? = null
    private var address: EditText? = null
    private var description: TextInputEditText? = null
    private var category: EditText? = null
    private var submitEdit: Button? = null
    private var floatingActionButton: FloatingActionButton? = null
    private var annotationApi: AnnotationPlugin? = null
    private var annotationConfig: AnnotationConfig? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var annotations = mutableListOf<PointAnnotationOptions>()
    private var coordinate: Point? = null
    private var editCamera:Button? = null
    private var editGallery:Button? = null
    private var locImage: Bitmap? = null

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

            image?.setImageBitmap(imageBitmap)
        }
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            locImage = imageBitmap
            image?.setImageBitmap(imageBitmap)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_food_mark)

        sheet = findViewById<FrameLayout>(R.id.sheet)
        mapView = findViewById<MapView>(R.id.editmapView)
        image = findViewById<ImageView>(R.id.markImage)
        editGallery= findViewById<Button>(R.id.editGallery)
        editCamera= findViewById<Button>(R.id.editCamera)
        title= findViewById<EditText>(R.id.editTitle)
        coordinates = findViewById<TextView>(R.id.editCoords)
        address = findViewById<EditText>(R.id.editLocation)
        description = findViewById<TextInputEditText>(R.id.editDescInput)
        category= findViewById<EditText>(R.id.editCategory)
        submitEdit = findViewById<Button>(R.id.btnSubmitEdit)

        floatingActionButton = findViewById<FloatingActionButton>(R.id.EditfocusLocation)
        val itemId = intent.getStringExtra("itemId");


        val behavior = BottomSheetBehavior.from<View>(
            sheet!!
        )
        behavior.peekHeight = 200
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val mDatabase = FirebaseDatabase.getInstance().getReference("FoodMarks")

        mDatabase.child(itemId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // Get the Item object
                val item = dataSnapshot.getValue(Item::class.java)
                val base64Image = item?.locationImage
                val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                // Set the text of the TextViews
                title?.setText(item?.title)
                coordinates?.text = "${item?.latitude}, ${item?.longitude}"
                address?.setText(item?.location)
                description?.setText(item?.description)
                category?.setText(item?.category)
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
                        locationComponentPlugin.addOnIndicatorBearingChangedListener(
                            onIndicatorBearingChangedListener
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

                createAnnotation(point)

            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })

        editCamera?.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraResultLauncher.launch(cameraIntent)
        }
        editGallery?.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            galleryResultLauncher.launch(galleryIntent)
        }

        submitEdit?.setOnClickListener {
            editLoc()
        }

    }


    private fun editLoc() {
        val itemId = intent.getStringExtra("itemId")
        val mDatabase = FirebaseDatabase.getInstance().getReference("FoodMarks")
        mDatabase.child(itemId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var item = dataSnapshot.getValue(Item::class.java)
                val title = title?.text.toString()
                val location = address?.text.toString()
                val description = description?.text.toString()
                val category = category?.text.toString()
                val latitude = coordinate?.latitude().toString()
                val longitude = coordinate?.longitude().toString()
                val locationImage = bitmapToBase64(locImage!!)
                item = Item(
                    locationImage,
                    title,
                    location,
                    latitude,
                    longitude,
                    description,
                    category,
                    itemId
                )
                mDatabase.child(itemId).setValue(item).addOnSuccessListener {
                    Toast.makeText(this@editFoodMark, "Location edited successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
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

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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
        coordinates?.text = "Latitude: ${point.latitude()} Longitude: ${point.longitude()}";
        coordinate = point;

    }
}