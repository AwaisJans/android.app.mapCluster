package com.example.myapplication

import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
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

    private fun readCoordinatesFromRawResource(rawResourceId: Int): List<LatLng> {
        val coordinates: MutableList<LatLng> = ArrayList()
        try {
            val resources = resources
            val inputStream = resources.openRawResource(rawResourceId)
            val json = convertStreamToString(inputStream)
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val latitude = jsonObject.getDouble("latitude")
                val longitude = jsonObject.getDouble("longitude")
                val coordinate = LatLng(latitude, longitude)
                coordinates.add(coordinate)
            }
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return coordinates
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val scanner = Scanner(`is`).useDelimiter("\\A")
        return if (scanner.hasNext()) scanner.next() else ""
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            // Read JSON data from the file
            val json = loadJSONFromRawResource(R.raw.new_json_file)
            val jsonObject = JSONObject(json)

            // Extract corner coordinates from the JSON data
            val corner1 = getLatLngFromJsonObject(jsonObject.getJSONObject("rectangle").getJSONObject("corner1"))
            val corner2 = getLatLngFromJsonObject(jsonObject.getJSONObject("rectangle").getJSONObject("corner2"))
            val corner3 = getLatLngFromJsonObject(jsonObject.getJSONObject("rectangle").getJSONObject("corner3"))
            val corner4 = getLatLngFromJsonObject(jsonObject.getJSONObject("rectangle").getJSONObject("corner4"))

            // Create a rectangle on the map
            val polygonOptions = PolygonOptions()
                    .add(corner1, corner2, corner3, corner4)
                    .strokeWidth(10f)
                    .strokeColor(-0xffff01) // Blue color for the rectangle border
                    .fillColor(-0x7fffff01) // Semi-transparent blue for the rectangle fill
            rectangle = mMap!!.addPolygon(polygonOptions)
            polygons.add(rectangle!!)


            // Move the camera to the center of the rectangle
            val centerLat = (corner1.latitude + corner3.latitude) / 2
            val centerLng = (corner1.longitude + corner3.longitude) / 2
            val centerLatLng = LatLng(centerLat, centerLng)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 20f))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mMap!!.setOnMapClickListener(OnMapClickListener { latLng ->
            for (polygon in polygons) {
                if (isPointInPolygon(latLng, polygon)) {
                    // Display a Toast message when a polygon is clicked
                    Toast.makeText(applicationContext, "Rectangle Clicked!", Toast.LENGTH_SHORT).show()
                    return@OnMapClickListener
                }
            }
        })
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