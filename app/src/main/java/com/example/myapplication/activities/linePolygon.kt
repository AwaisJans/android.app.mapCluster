package com.example.myapplication.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.util.Scanner

class linePolygon : AppCompatActivity(), OnMapReadyCallback, OnMapClickListener{
    private var mMap: GoogleMap? = null
    private val marker: Marker? = null
    private var mapView: MapView? = null
    private val markers: List<Marker> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)

        mapView!!.getMapAsync(this@linePolygon)
        mapView!!.onCreate(savedInstanceState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            mMap!!.setOnMapClickListener(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Error....", Toast.LENGTH_SHORT).show()
        }

        // drawMarkersonMap()



    }





    override fun onMapClick(latLng: LatLng) {

    }

    private fun drawMarkersonMap() {

        val latLngArray = readCoordinatesFromRawMarkers(R.raw.new_json_file)

        for (latLng in latLngArray) {
            mMap!!.addMarker(MarkerOptions().position(latLng))
        }

        if (latLngArray.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            for (latLng in latLngArray) {
                boundsBuilder.include(latLng)
            }
            val bounds = boundsBuilder.build()
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50)
            mMap!!.moveCamera(cameraUpdate)
        }


        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(33.6844, 72.9902), 5f))

    }

    private fun readCoordinatesFromRawMarkers(resourceId: Int): ArrayList<LatLng> {
        val coordinates = ArrayList<LatLng>()
        try {
            val jsonString = Scanner(resources.openRawResource(resourceId)).useDelimiter("\\A").next()
            val jsonObject = JSONObject(jsonString)
            val markerObject1 = jsonObject.getJSONObject("markers")
            val jsonArray = markerObject1.getJSONArray("coordinates")
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val latitude = item.getDouble("latitude")
                val longitude = item.getDouble("longitude")
                coordinates.add(LatLng(latitude, longitude))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return coordinates
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