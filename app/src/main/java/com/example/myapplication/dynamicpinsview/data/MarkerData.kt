package com.example.myapplication.dynamicpinsview.data

typealias MarkerData = Map<String, Any>

val MarkerData.lon: Double
    get() = this["lon"]!! as Double

val MarkerData.lat: Double
    get() = this["lat"]!! as Double
