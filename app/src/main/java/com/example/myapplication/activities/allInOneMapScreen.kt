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
import com.example.myapplication.dataClass.GeometeryObjectModel
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
import com.google.android.gms.maps.GoogleMap.OnPolygonClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.util.Scanner
import kotlin.math.absoluteValue


class allInOneMapScreen : AppCompatActivity(), OnMapReadyCallback, OnMapClickListener,
    MapMarkersRenderer.Callback, OnPolygonClickListener {
    private var mMap: GoogleMap? = null
    private var mapView: MapView? = null
    private var latLngArray: Array<LatLng>? = null
    private var polygon: Polygon? = null
    private var circle: Circle? = null
    private var key: String? = null
    private var rectangle: Polygon? = null
    private val rectangles: MutableList<Polygon> = ArrayList()
    private val circles: MutableList<Circle> = ArrayList()


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

        /*addNew(this@allInOneMapScreen, R.raw.new_json_file)


        scope.launch {
            boundariesFlow.collect { update -> reloadData(update) }
        }*/

    }

    fun addNew(context: Context, resourceId: Int) {
        val inputStream = context.resources.openRawResource(resourceId)
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(jsonString)
        val markerObject1 = jsonObject.getJSONObject("markers")
        val jsonArray = markerObject1.getJSONArray("coordinates")

        for (i in 0 until jsonArray.length()) {
            val coordinate = jsonArray.getJSONObject(i)
            val latitude = coordinate.getDouble("latitude")
            val longitude = coordinate.getDouble("longitude")

            val markerData = mapOf("latitude" to latitude, "longitude" to longitude)
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

        //drawLinesofTwoCordinates()
        // drawCircleofGivenCordinates()

        //drawRectangleofGivenCordinates() --> No Longer useful
        //rectangle1()                     --> No Longer useful
        //rectangle2()                     --> No Longer useful





        jsonParsing()
        Log.e("Jans", "HW")
        //drawRectDynamic(R.raw.new_json_file, "rectangle")
        //drawRectDynamic(R.raw.new_json_file, "z_shape")
        //drawRectDynamic(R.raw.new_json_file, "zig_zag_shape")

        mMap!!.setOnMapClickListener(OnMapClickListener { latLng ->
            for (polygon in polygons) {
                if (isPointInPolygon(latLng, polygon)) {
                    // Handle the click on the polygon here
                    Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()

                }
            }
            /*// Click listener for rectangle 1
             for (polygon in polygons) {
                 if (isPointInPolygon(latLng, polygon)) {
                     // Display a Toast message when a polygon is clicked
                     Toast.makeText(applicationContext, "rectangle Clicked!", Toast.LENGTH_SHORT)
                         .show()
                     return@OnMapClickListener
                 }
             }
             // Click listener for rectangle 2

             for (polygon in polygons1) {
                 if (isPointInPolygon(latLng, polygon)) {
                     // Display a Toast message when a polygon is clicked
                     Toast.makeText(applicationContext, "z_shape Clicked!", Toast.LENGTH_SHORT)
                         .show()
                     return@OnMapClickListener
                 }
             }
             // Click listener for rectangle 3

             for (polygon in polygons3) {
                 if (isPointInPolygon(latLng, polygon)) {
                     // Display a Toast message when a polygon is clicked
                     Toast.makeText(applicationContext, "zig_zag_shape Clicked!", Toast.LENGTH_SHORT)
                         .show()
                     return@OnMapClickListener
                 }
             }*/
        })

        //drawMarkersonMap()
    }

    private fun jsonParsing() {


        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(34.014581028940114, 71.52701810263859)),)
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(5f))

        val gson = GsonBuilder().create()?.let { it } ?: return

        try {
            val geometeryItems = resources.openRawResource(R.raw.new_json_file)
                .bufferedReader().use { it.readText() }

            val geometerySingle =
                gson.fromJson(geometeryItems, GeometeryObjectModel::class.java)?.let { it }
                    ?: return

            for (i in 0 until geometerySingle.itemsEntity.size) {
                // add item entity in array

                Log.e("items", "${geometerySingle.itemsEntity[i].type}")
                val typeItem = geometerySingle.itemsEntity[i].type

                if (typeItem == "circle") {
                    for (f in 0 until geometerySingle.itemsEntity[i].coordinatesEntity.size) {
                        // add item entity in array

                        Log.e("itemCors", "${typeItem}")
                        val cordinate = geometerySingle.itemsEntity[i].coordinatesEntity[f]

                        val latitude = cordinate.latitude
                        val longitude = cordinate.longitude

                        Log.e(
                            "newLatLng",
                            "lat -> " + latitude.toString() +
                                    "long -> " + longitude.toString()
                        )
                        val coordinates: MutableList<LatLng> = ArrayList()

                        coordinates.add(LatLng(latitude, longitude))

                        drawCircleofGivenCordinates(coordinates.toTypedArray())


                        // val fetchCircle =  CircleOptions
                        //  circles.add(fetchCircle)


                    }
                }

            }


        } catch (e: JSONException) {
            // if output is not in json format
            // json exception
            e.printStackTrace()

        }
        catch (exception: IllegalStateException) {
            // response is not json format
            // json exception

            exception.printStackTrace()
            Log.e("showInternetErrorDialog", "hhh")


        }
        catch (exception: JsonSyntaxException) {
            // response is not json format
            // json exception

            exception.printStackTrace()
            Log.e("showInternetErrorDialog", "syntax")

        }
    }


    private fun isPointInPolygon(point: LatLng, polygon: Polygon): Boolean {
        val vertices = polygon.points

        val latLngList = vertices.map { it }

        return PolyUtil.containsLocation(point, latLngList, false)
    }

    override fun onMapClick(latLng: LatLng) {

    }

    // Define a function to get the location name from LatLng
    fun getLocationNameFromLatLng(lat: Double, lon: Double): String {
        val geocoder =
            Geocoder(this@allInOneMapScreen) // Replace 'context' with your Android application context

        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val locationName =
                    address.getAddressLine(0) // This could be any part of the address, e.g., locality, admin area, etc.
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

        clusterManager.setOnClusterClickListener(object :
            ClusterManager.OnClusterClickListener<MapMarker> {
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


    fun calculateRadius(center: LatLng, corner: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            center.latitude,
            center.longitude,
            corner.latitude,
            corner.longitude,
            results
        )
        return results[0].toDouble()
    }


    private fun drawCircleofGivenCordinates(arrayLatLng: Array<LatLng>) {
        val circleOptions = CircleOptions()

        val builder = LatLngBounds.builder()
        for (latLng in arrayLatLng) {
            builder.include(latLng)
        }
        val bounds = builder.build()

        val center = bounds.center

        val radius = calculateRadius(center, bounds.southwest)

        circleOptions.center(center)
            .radius(radius)
            .strokeWidth(2f)
            .strokeColor(Color.BLACK)
            .fillColor(0x30ff0000)

        circle = mMap?.addCircle(circleOptions)

        circle!!.isClickable = true

        mMap!!.setOnCircleClickListener {
            Toast.makeText(this, "You clicked the circle!", Toast.LENGTH_SHORT).show()
        }



        mMap!!.setOnMapClickListener(this)
        if (latLngArray!!.size > 0) {
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

    override fun onPolygonClick(p0: Polygon) {
    }
}