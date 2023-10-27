package com.example.myapplication.mapClusterUtils.data

typealias MarkerData = Map<String, Any>

val MarkerData.lon: Double
    get() = this["longitude"]!! as Double

val MarkerData.lat: Double
    get() = this["latitude"]!! as Double
