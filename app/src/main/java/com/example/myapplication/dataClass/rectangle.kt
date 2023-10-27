package com.example.myapplication.dataClass

import java.io.Serializable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GeometeryObjectModel(
    @Expose
    @SerializedName("items")
    var itemsEntity: List<ItemsEntity>,
    @Expose
    @SerializedName("status")
    val status: String
) : Serializable

data class ItemsEntity(
    @Expose
    @SerializedName("internalKey")
    val internalkey: String,
    @Expose
    @SerializedName("type")
    val type: String,
    @Expose
    @SerializedName("coordinates")
    var coordinatesEntity: List<CoordinatesEntity>
)

data class CoordinatesEntity(
    @Expose
    @SerializedName("latitude")
    val latitude: Double,
    @Expose
    @SerializedName("longitude")
    val longitude: Double
)
