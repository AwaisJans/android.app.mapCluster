package com.example.myapplication.activities

import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.mapClusterUtils.data.MarkerData
import com.example.myapplication.mapClusterUtils.data.lat
import com.example.myapplication.mapClusterUtils.data.lon
import com.example.myapplication.mapClusterUtils.listener.BoundariesListener
import com.example.myapplication.mapClusterUtils.map.MapHolder
import com.example.myapplication.mapClusterUtils.map.MapMarker
import com.example.myapplication.mapClusterUtils.map.MapMarkersRenderer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Scanner
import kotlin.math.absoluteValue


class allInOneMapScreen : AppCompatActivity(), OnMapReadyCallback, OnMapClickListener,
    MapMarkersRenderer.Callback {
    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    lateinit var latLngArray: Array<LatLng>
    private var polygon: Polygon? = null
    private var circle: Circle? = null
    private var rectangle: Polygon? = null
    private val polygons: MutableList<Polygon> = ArrayList()



    private val mapHolder = MapHolder()
    private val boundariesFlow = MutableSharedFlow<LatLngBounds>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var currentMarkersSet: MutableSet<MapMarker> = mutableSetOf()
    private val locations: MutableList<MarkerData> = mutableListOf()

    private lateinit var mapRenderer: MapMarkersRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)

        mapView!!.getMapAsync(this@allInOneMapScreen)
        mapView!!.onCreate(savedInstanceState)

        // cluster code

        addNew(this@allInOneMapScreen, R.raw.new_json_file)


        scope.launch {
            boundariesFlow.collect { update -> reloadData(update) }
        }

    }

    fun addNew(context: Context, resourceId: Int) {
        val inputStream = context.resources.openRawResource(resourceId)
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(jsonString)
        val markerObject1 = jsonObject.getJSONObject("markers")
        val jsonArray = markerObject1.getJSONArray("coordinates")

        for (i in 0 until jsonArray.length()) {
            val coordinate = jsonArray.getJSONObject(i)
            val latitude = coordinate.getDouble("lat")
            val longitude = coordinate.getDouble("lon")

            val markerData = mapOf("lat" to latitude, "lon" to longitude)
            locations.add(markerData)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            mMap!!.setOnMapClickListener(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Error....", Toast.LENGTH_SHORT).show()
        }

        drawLinesofTwoCordinates()
        drawCircleofGivenCordinates()
        drawRectangleofGivenCordinates()
        drawMarkersonMap()


    }
    // Define a function to get the location name from LatLng
    fun getLocationNameFromLatLng(lat: Double, lon: Double): String {
        val geocoder = Geocoder(this@allInOneMapScreen) // Replace 'context' with your Android application context

        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val locationName = address.getAddressLine(0) // This could be any part of the address, e.g., locality, admin area, etc.
                return locationName
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }

    private fun reloadData(bounds: LatLngBounds) {
        fun LatLngBounds.includesLocation(lat: Double, lon: Double): Boolean {
            return this.northeast.latitude > lat && this.southwest.latitude < lat &&
                    this.northeast.longitude > lon && this.southwest.longitude < lon

        }

        val locationsInArea = locations.filter { bounds.includesLocation(it.lat, it.lon) }

        locationsInArea.map { item ->

            MapMarker(
                location = LatLng(item.lat, item.lon),
                titleText = getLocationNameFromLatLng(item.lat, item.lon),
                icon = MapMarker.Icon.Placeholder(""),
                pinColor = COLORS[item.hashCode().absoluteValue.rem(10)]
            )
        }.apply { setMarkers(this) }
    }

    private fun setMarkers(markers: List<MapMarker>) {
        val newMarkersSet = markers.toMutableSet()
        val removedElements = currentMarkersSet - newMarkersSet
        val addedElements = newMarkersSet - currentMarkersSet
        mapHolder.executeOnClusterManager { manager ->
            manager.apply {
                removedElements.forEach { removeItem(it) }
                addedElements.forEach { addItem(it) }
                cluster()
            }
        }
        currentMarkersSet = newMarkersSet
    }

    override fun onImageLoaded(icon: MapMarker.Icon) {
        mapHolder.executeOnClusterManager { manager ->
            currentMarkersSet.forEach { marker ->
                if (marker.icon.url == icon.url) {
                    Log.v("MapMarkersRenderer", "marker added ${icon.url}")
                    manager.updateItem(marker)
                    manager.cluster()
                }
            }
        }
    }

    companion object {
        private val COLORS = listOf(
            Color.parseColor("#6a0136"),
            Color.parseColor("#bfab25"),
            Color.parseColor("#b81365"),
            Color.parseColor("#026c7c"),
            Color.parseColor("#055864"),
            Color.parseColor("#58355e"),
            Color.parseColor("#e03616"),
            Color.parseColor("#fff689"),
            Color.parseColor("#cfffb0"),
            Color.parseColor("#5998c5"),
        )
    }

    private fun drawMarkersonMap() {
        mMap!!.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = true
        }

        val boundariesListener = BoundariesListener(mMap!!, boundariesFlow::tryEmit)
        val clusterManager = ClusterManager<MapMarker>(this, mMap)

        mapRenderer = MapMarkersRenderer(
            context = this,
            callback = this@allInOneMapScreen,
            map = mMap!!,
            clusterManager = clusterManager
        )
        clusterManager.renderer = mapRenderer
        mapHolder.initialize(
            msMap = mMap!!,
            clusterManager = clusterManager,
            boundariesListener = boundariesListener,
        )

        clusterManager.setOnClusterClickListener(object : ClusterManager.OnClusterClickListener<MapMarker> {
            override fun onClusterClick(cluster: Cluster<MapMarker>): Boolean {
                if (cluster.items.size > 1) {
                   // Toast.makeText(this@allInOneMapScreen,"hello",Toast.LENGTH_SHORT).show()
                    mMap!!.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            cluster.position, Math.floor(
                                (mMap!!
                                    .getCameraPosition().zoom + 5).toDouble()
                            ).toFloat()
                        ), 300,
                        null
                    )
                }
                return true
            }
        })

    }

    private fun drawRectangleofGivenCordinates() {
        try {
            // Read JSON data from the file
            val json = loadJSONFromRawResource(R.raw.new_json_file)
            val jsonObject = JSONObject(json)

            // Extract corner coordinates from the JSON data
            val corner1 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("rectangle").getJSONObject("corner1")
            )
            val corner2 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("rectangle").getJSONObject("corner2")
            )
            val corner3 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("rectangle").getJSONObject("corner3")
            )
            val corner4 = getLatLngFromJsonObject(
                jsonObject.getJSONObject("rectangle").getJSONObject("corner4")
            )

            val polygonOptions = PolygonOptions()
                .add(corner1, corner2, corner3, corner4)
                .strokeWidth(10f)
                .strokeColor(-0xffff01) // Blue color for the rectangle border
                .fillColor(-0x7fffff01) // Semi-transparent blue for the rectangle fill
            rectangle = mMap!!.addPolygon(polygonOptions)
            polygons.add(rectangle!!)

            val centerLat = (corner1.latitude + corner3.latitude) / 2
            val centerLng = (corner1.longitude + corner3.longitude) / 2
            val centerLatLng = LatLng(centerLat, centerLng)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mMap!!.setOnMapClickListener(OnMapClickListener { latLng ->
            for (polygon in polygons) {
                if (isPointInPolygon(latLng, polygon)) {
                    // Display a Toast message when a polygon is clicked
                    Toast.makeText(applicationContext, "Rectangle Clicked!", Toast.LENGTH_SHORT)
                        .show()
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

    private fun drawCircleofGivenCordinates() {
        try {
            val circleOptions = CircleOptions()
            latLngArray = readCoordinatesFromRawResource(R.raw.new_json_file)
            val builder = LatLngBounds.builder()
            for (latLng in latLngArray) {
                builder.include(latLng)
            }
            val bounds = builder.build()
            val center = bounds.center
            val results = FloatArray(1)
            Location.distanceBetween(
                center.latitude,
                center.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude,
                results
            )
            val radius = results[0].toDouble()
            circleOptions.center(center)
                .radius(radius)
                .strokeWidth(2f)
                .strokeColor(Color.BLACK)
                .fillColor(0x30ff0000)

            circle = mMap?.addCircle(circleOptions)

            mMap!!.setOnMapClickListener(this)
            if (latLngArray.size > 0) {
            }
        } catch (e: NullPointerException) {
            Toast.makeText(this, "image is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawLinesofTwoCordinates() {
        val marker1 = readMarkerCoordinateFromRawResource(R.raw.new_json_file, "point1") // peshawar
        val marker2 = readMarkerCoordinateFromRawResource(R.raw.new_json_file, "point2") // kohat
        val polygonOptions = PolygonOptions()
            .add(marker1)
            .add(marker2)
            .strokeWidth(10f) // Set the stroke width
            .strokeColor(-0xffff01) // Set the stroke color (blue)
            .fillColor(-0x7fffff01) // Set the fill color with transparency (blue with 50% transparency)
        polygon = mMap!!.addPolygon(polygonOptions)


        val builder = LatLngBounds.Builder()
        for (point in polygonOptions.points) {
            builder.include(point!!)
        }
        val bounds = builder.build()
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50)) // The "50" is padding
        mMap!!.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                marker1!!,
                8.5f
            )
        )
    }

    private fun readCoordinatesFromRawResource(rawResourceId: Int): Array<LatLng> {
        val coordinates: MutableList<LatLng> = ArrayList()
        try {
            val resources = resources
            val inputStream = resources.openRawResource(rawResourceId)
            val scanner = Scanner(inputStream).useDelimiter("\\A")
            val json = if (scanner.hasNext()) scanner.next() else ""
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

    private fun readMarkerCoordinateFromRawResource(
        rawResourceId: Int,
        markerName: String
    ): LatLng? {
        try {
            val resources = resources
            val inputStream = resources.openRawResource(rawResourceId)
            val scanner = Scanner(inputStream).useDelimiter("\\A")
            val json = if (scanner.hasNext()) scanner.next() else ""
            val jsonObject = JSONObject(json)
            val markerObject1 = jsonObject.getJSONObject("line")
            val markerObject = markerObject1.getJSONObject(markerName)
            val latitude = markerObject.getDouble("latitude")
            val longitude = markerObject.getDouble("longitude")
            return LatLng(latitude, longitude)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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


        scope.coroutineContext.cancelChildren()
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