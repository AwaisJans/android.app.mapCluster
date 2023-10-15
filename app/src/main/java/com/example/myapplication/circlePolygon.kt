package com.example.myapplication

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.Scanner

class circlePolygon : AppCompatActivity(), OnMapReadyCallback, OnMapClickListener {
    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    private val markers: List<Marker> = ArrayList()
    lateinit var latLngArray: Array<LatLng>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)

        // Initialize the map settings
        mapView!!.getMapAsync(this@circlePolygon)
        mapView!!.onCreate(savedInstanceState)
    }

    private var circle: Circle? = null


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            /*val circleOptions = CircleOptions()
            latLngArray = readCoordinatesFromRawResource(R.raw.new_json_file)
            for (latLng in latLngArray) {
                circleOptions.center(latLng)
                        .radius(7400.0) // Radius in meters
                        .strokeWidth(2f)
                        .strokeColor(Color.BLACK)
                        .fillColor(0x30ff0000)
                circle = mMap!!.addCircle(circleOptions)
            }
            mMap!!.setOnMapClickListener(this)


            // Move the camera to the last LatLng in the array
            if (latLngArray.size > 0) {
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngArray[latLngArray.size - 1], 10f))
            }*/


            val circleOptions = CircleOptions()
            latLngArray = readCoordinatesFromRawResource(R.raw.new_json_file)

// Find the bounds that encompass all LatLng coordinates
            val builder = LatLngBounds.builder()
            for (latLng in latLngArray) {
                builder.include(latLng)
            }
            val bounds = builder.build()

// Calculate the center of the bounding box
            val center = bounds.center

// Calculate the radius based on the distance from the center to a corner of the bounding box
            val radius = calculateRadius(center, bounds.southwest)

// Create a single circle that encompasses all coordinates
            circleOptions.center(center)
                    .radius(radius)
                    .strokeWidth(2f)
                    .strokeColor(Color.BLACK)
                    .fillColor(0x30ff0000)

            circle = mMap?.addCircle(circleOptions)
            if (latLngArray.size > 0) {
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngArray[latLngArray.size - 1], 10f))
            }

        } catch (e: NullPointerException) {
            Toast.makeText(this, "image is null", Toast.LENGTH_SHORT).show()
        }
    }
    fun calculateRadius(center: LatLng, corner: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(center.latitude, center.longitude, corner.latitude, corner.longitude, results)
        return results[0].toDouble()
    }
    private fun readCoordinatesFromRawResource(rawResourceId: Int): Array<LatLng> {
        val coordinates: MutableList<LatLng> = ArrayList()
        try {
            val resources = resources
            val inputStream = resources.openRawResource(rawResourceId)
            val json = convertStreamToString(inputStream)
            var jsonObject = JSONObject(json)
            val markerObject1 = jsonObject.getJSONObject("circle")
            val coordinatesArray = markerObject1.getJSONArray("coordinates")
            for (i in 0 until coordinatesArray.length()) {
                jsonObject = coordinatesArray.getJSONObject(i)
                val latitude = jsonObject.getDouble("latitude")
                val longitude = jsonObject.getDouble("longitude")
                val coordinate = LatLng(latitude, longitude)
                coordinates.add(coordinate)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return coordinates.toTypedArray()
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val scanner = Scanner(`is`).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else ""
    }

    override fun onMapClick(latLng: LatLng) {
        for (circleCenter in latLngArray) {
            if (SphericalUtil.computeDistanceBetween(latLng, circleCenter) <= 7400) {
                // Display a Toast message when a circle is clicked
                Toast.makeText(this, "New York Circle Clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }
}