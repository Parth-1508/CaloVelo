package com.calovelo.app.domain.util

import kotlin.math.roundToInt

/**
 * Google Encoded Polyline Algorithm (precision 5, ~1.1 m resolution).
 * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 *
 * Points are (latitude, longitude) pairs in decimal degrees.
 */
object PolylineEncoder {

    private const val PRECISION = 1e5

    fun encode(points: List<Pair<Double, Double>>): String {
        val result = StringBuilder(points.size * 8)
        var previousLat = 0
        var previousLng = 0
        for ((latitude, longitude) in points) {
            require(latitude in -90.0..90.0 && longitude in -180.0..180.0) {
                "Coordinate out of range: ($latitude, $longitude)"
            }
            val lat = (latitude * PRECISION).roundToInt()
            val lng = (longitude * PRECISION).roundToInt()
            appendEncoded(lat - previousLat, result)
            appendEncoded(lng - previousLng, result)
            previousLat = lat
            previousLng = lng
        }
        return result.toString()
    }

    fun decode(encoded: String): List<Pair<Double, Double>> {
        val points = ArrayList<Pair<Double, Double>>(encoded.length / 6)
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            val latDelta = readValue(encoded, index)
            index = latDelta.nextIndex
            val lngDelta = readValue(encoded, index)
            index = lngDelta.nextIndex
            lat += latDelta.value
            lng += lngDelta.value
            points.add((lat / PRECISION) to (lng / PRECISION))
        }
        return points
    }

    private fun appendEncoded(value: Int, out: StringBuilder) {
        var remaining = if (value < 0) (value shl 1).inv() else value shl 1
        while (remaining >= 0x20) {
            out.append((((remaining and 0x1f) or 0x20) + 63).toChar())
            remaining = remaining ushr 5
        }
        out.append((remaining + 63).toChar())
    }

    private class DecodedValue(val value: Int, val nextIndex: Int)

    private fun readValue(encoded: String, startIndex: Int): DecodedValue {
        var index = startIndex
        var result = 0
        var shift = 0
        var chunk: Int
        do {
            require(index < encoded.length) { "Malformed polyline: truncated at index $index" }
            chunk = encoded[index++].code - 63
            require(chunk in 0..63) { "Malformed polyline: invalid character at index ${index - 1}" }
            result = result or ((chunk and 0x1f) shl shift)
            shift += 5
        } while (chunk >= 0x20)
        val value = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
        return DecodedValue(value, index)
    }
}
