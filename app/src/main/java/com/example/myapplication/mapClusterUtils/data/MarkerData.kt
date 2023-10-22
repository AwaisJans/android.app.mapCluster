package com.example.myapplication.mapClusterUtils.data

typealias MarkerData = Map<String, Any>

val MarkerData.lon: Double
    get() = this["lon"]!! as Double

val MarkerData.lat: Double
    get() = this["lat"]!! as Double
