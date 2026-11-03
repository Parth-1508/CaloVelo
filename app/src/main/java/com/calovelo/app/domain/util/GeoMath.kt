package com.calovelo.app.domain.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoMath {
    private const val EARTH_RADIUS_METERS = 6_371_008.8

    /** Great-circle distance between two coordinates using the haversine formula. */
    fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lng2 - lng1)
        val a = sin(dPhi / 2) * sin(dPhi / 2) +
            cos(phi1) * cos(phi2) * sin(dLambda / 2) * sin(dLambda / 2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}
