package com.calovelo.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineEncoderTest {

    // Canonical example from Google's Encoded Polyline Algorithm documentation.
    private val googleExamplePoints = listOf(
        38.5 to -120.2,
        40.7 to -120.95,
        43.252 to -126.453
    )
    private val googleExampleEncoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"

    @Test
    fun encode_matchesGoogleReferenceExample() {
        assertEquals(googleExampleEncoded, PolylineEncoder.encode(googleExamplePoints))
    }

    @Test
    fun decode_matchesGoogleReferenceExample() {
        val decoded = PolylineEncoder.decode(googleExampleEncoded)
        assertEquals(googleExamplePoints.size, decoded.size)
        googleExamplePoints.zip(decoded).forEach { (expected, actual) ->
            assertEquals(expected.first, actual.first, 1e-5)
            assertEquals(expected.second, actual.second, 1e-5)
        }
    }

    @Test
    fun roundTrip_preservesFivePlacePrecision() {
        val route = listOf(
            18.52043 to 73.85674,
            18.52051 to 73.85690,
            18.52049 to 73.85611,
            -33.86882 to 151.20929,
            0.0 to 0.0,
            -0.00001 to -0.00001
        )
        val decoded = PolylineEncoder.decode(PolylineEncoder.encode(route))
        assertEquals(route.size, decoded.size)
        route.zip(decoded).forEach { (expected, actual) ->
            assertEquals(expected.first, actual.first, 1e-5)
            assertEquals(expected.second, actual.second, 1e-5)
        }
    }

    @Test
    fun emptyInput_roundTripsToEmpty() {
        assertEquals("", PolylineEncoder.encode(emptyList()))
        assertTrue(PolylineEncoder.decode("").isEmpty())
    }

    @Test
    fun encode_rejectsOutOfRangeCoordinates() {
        assertThrows(IllegalArgumentException::class.java) {
            PolylineEncoder.encode(listOf(91.0 to 0.0))
        }
    }

    @Test
    fun decode_rejectsTruncatedInput() {
        assertThrows(IllegalArgumentException::class.java) {
            PolylineEncoder.decode(googleExampleEncoded.dropLast(3))
        }
    }
}
