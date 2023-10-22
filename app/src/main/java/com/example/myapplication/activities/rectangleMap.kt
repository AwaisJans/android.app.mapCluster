package com.example.myapplication.activities

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Color
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
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.Scanner

class rectangleMap : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    private val markers: List<Marker> = ArrayList()
    private var rectangle: Polygon? = null
    private val polygons: MutableList<Polygon> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)

        // Initialize the map settings
        mapView!!.getMapAsync(this@rectangleMap)
        mapView!!.onCreate(savedInstanceState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap




        /*rectangle1()
        mMap!!.setOnMapClickListener(OnMapClickListener { latLng ->
            // Click listener for rectangle 1
            for (polygon in polygons) {
                if (isPointInPolygon(latLng, polygon)) {
                    // Display a Toast message when a polygon is clicked
                    Toast.makeText(applicationContext, "Rectangle 1 Clicked!", Toast.LENGTH_SHORT)
                        .show()
                    return@OnMapClickListener
                }
            }
        })*/
    }


    private fun rectangle1() {
        try {
            // Read JSON data from the file
            val json = loadJSONFromRawResource(R.raw.new_json_file)
            val jsonObject = JSONObject(json)

            // Extract coordinate coordinates from the JSON data
            val coordinate1 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate1")
            )
            val coordinate2 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate2")
            )
            val coordinate3 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate3")
            )
            val coordinate4 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate4")
            )
            val coordinate5 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate5")
            )
            val coordinate6 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate6")
            )
            val coordinate7 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate7")
            )
            val coordinate8 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate8")
            )
            val coordinate9 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("zig_zag_shape").getJSONObject("coordinate9")
            )

            // Create a rectangle on the map
            val polygonOptions = PolygonOptions().add(
                coordinate1,
                coordinate2,
                coordinate3,
                coordinate4,
                coordinate5,
                coordinate6,
                coordinate7,
                coordinate8,
                coordinate9
            ).strokeWidth(10f).strokeColor(-0xffff01) // Blue color for the rectangle border
                .fillColor(resources.getColor(R.color.black)) // Semi-transparent blue for the rectangle fill
            rectangle = mMap!!.addPolygon(polygonOptions)
            polygons.add(rectangle!!)


            // Move the camera to the center of the rectangle
            val centerLat = (coordinate1.latitude + coordinate8.latitude) / 2
            val centerLng = (coordinate1.longitude + coordinate8.longitude) / 2
            val centerLatLng = LatLng(centerLat, centerLng)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 10f))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    private fun loadJSONFromRawResource(rawResourceId: Int): String? {
        return try {
            val resources = resources
            val inputStream = resources.openRawResource(rawResourceId)
            val scanner = Scanner(inputStream).useDelimiter("\\A")
            if (scanner.hasNext()) scanner.next() else ""
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Throws(JSONException::class)
    private fun getLatLngFromJsonObject(jsonObject: JSONObject): LatLng {
        val latitude = jsonObject.getDouble("latitude")
        val longitude = jsonObject.getDouble("longitude")
        return LatLng(latitude, longitude)
    }

    // Check if a point is inside a polygon
    private fun isPointInPolygon(point: LatLng, polygon: Polygon): Boolean {
        val polygonPoints = polygon.points
        var crossings = 0
        for (i in polygonPoints.indices) {
            val a = polygonPoints[i]
            val b = polygonPoints[(i + 1) % polygonPoints.size]
            if (a.latitude > point.latitude != b.latitude > point.latitude && point.longitude < (b.longitude - a.longitude) * (point.latitude - a.latitude) / (b.latitude - a.latitude) + a.longitude) {
                crossings++
            }
        }
        return crossings % 2 != 0
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