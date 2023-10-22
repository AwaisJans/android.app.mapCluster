package com.example.myapplication.mapClusterUtils.map

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class MapMarker(
    val icon: Icon,
    val titleText: String,
    @ColorInt val pinColor: Int,
    val location: LatLng
) : ClusterItem {

    override fun getPosition(): LatLng = location

    override fun getTitle(): String? = null

    // title getter/setter
    override fun getSnippet(): String? = null

    sealed interface Icon {

        // url initialization
        val url: String

        data class Placeholder(override val url: String) : Icon

        data class BitmapIcon(override val url: String, val image: Bitmap) : Icon
    }
}