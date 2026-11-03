package com.calovelo.app.domain.model

/** Framework-free coordinate pair so the domain layer needs no Maps SDK dependency. */
data class LatLng(val latitude: Double, val longitude: Double) {
    fun toPair(): Pair<Double, Double> = latitude to longitude
}

fun Pair<Double, Double>.toLatLng(): LatLng = LatLng(first, second)
