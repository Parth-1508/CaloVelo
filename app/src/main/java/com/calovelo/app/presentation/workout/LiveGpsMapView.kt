package com.calovelo.app.presentation.workout

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.calovelo.app.domain.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng as GmsLatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

private const val STRAVA_DARK_MAP_STYLE = """
[
  {"elementType": "geometry", "stylers": [{"color": "#17263c"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#8ec3b9"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#1a3646"}]},
  {"featureType": "administrative.country", "elementType": "geometry.stroke", "stylers": [{"color": "#4b6878"}]},
  {"featureType": "administrative.land_parcel", "elementType": "labels.text.fill", "stylers": [{"color": "#64779e"}]},
  {"featureType": "administrative.province", "elementType": "geometry.stroke", "stylers": [{"color": "#4b6878"}]},
  {"featureType": "landscape.man_made", "elementType": "geometry.stroke", "stylers": [{"color": "#334e87"}]},
  {"featureType": "landscape.natural", "elementType": "geometry", "stylers": [{"color": "#021019"}]},
  {"featureType": "poi", "elementType": "geometry", "stylers": [{"color": "#283d6a"}]},
  {"featureType": "poi", "elementType": "labels.text.fill", "stylers": [{"color": "#6f9ba5"}]},
  {"featureType": "poi", "elementType": "labels.text.stroke", "stylers": [{"color": "#1d2c4d"}]},
  {"featureType": "poi.park", "elementType": "geometry.fill", "stylers": [{"color": "#023e58"}]},
  {"featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [{"color": "#3C768D"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#304a7d"}]},
  {"featureType": "road", "elementType": "labels.text.fill", "stylers": [{"color": "#98a5be"}]},
  {"featureType": "road", "elementType": "labels.text.stroke", "stylers": [{"color": "#1d2c4d"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#2c6675"}]},
  {"featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [{"color": "#255763"}]},
  {"featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [{"color": "#b0d5ce"}]},
  {"featureType": "road.highway", "elementType": "labels.text.stroke", "stylers": [{"color": "#023e58"}]},
  {"featureType": "transit", "elementType": "labels.text.fill", "stylers": [{"color": "#98a5be"}]},
  {"featureType": "transit", "elementType": "labels.text.stroke", "stylers": [{"color": "#1d2c4d"}]},
  {"featureType": "transit.line", "elementType": "geometry.fill", "stylers": [{"color": "#283d6a"}]},
  {"featureType": "transit.station", "elementType": "geometry", "stylers": [{"color": "#3a4762"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#0e1626"}]},
  {"featureType": "water", "elementType": "labels.text.fill", "stylers": [{"color": "#4e6d70"}]}
]
"""

@Composable
fun LiveGpsMapView(
    currentLocation: LatLng?,
    routePoints: List<LatLng>,
    elapsedTimeFormatted: String = "00:00",
    splitPaceFormatted: String = "--:--",
    distanceKmFormatted: String = "0",
    gpsAcquired: Boolean = true,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val defaultCoord = GmsLatLng(18.5204, 73.8567) // Default Pune fallback
    val initialPos = currentLocation?.let { GmsLatLng(it.latitude, it.longitude) } ?: defaultCoord

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 16.5f)
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(GmsLatLng(loc.latitude, loc.longitude))
            )
        }
    }

    val mapProperties = remember(hasFineLocation) {
        MapProperties(
            isMyLocationEnabled = hasFineLocation,
            mapStyleOptions = MapStyleOptions(STRAVA_DARK_MAP_STYLE)
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = false
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Interactive Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            if (routePoints.size > 1) {
                val gmsPoints = routePoints.map { GmsLatLng(it.latitude, it.longitude) }
                // Strava electric cyan glowing polyline
                Polyline(
                    points = gmsPoints,
                    color = Color(0xFF00D2FF),
                    width = 14f
                )
            }
        }

        // Top Header Overlay: Back button + Workout Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Strava Banner Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color(0xFFFC4C02).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFC4C02)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            "CaloVelo GPS Tracker",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            "Live street polyline & HUD",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Floating Map Controls on Right (Layers, 3D, Re-center)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Layers Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Layers, contentDescription = "Layers", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // 3D Tilt Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("3D", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }

            // Re-center Location Crosshair Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable {
                        currentLocation?.let { loc ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        GmsLatLng(loc.latitude, loc.longitude),
                                        17f
                                    )
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Re-center", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }

        // Strava Middle HUD Card (Time, Split avg, Distance, GPS signal)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F1B12))
                .border(1.dp, Color(0xFF1B3D23), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // GPS Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.SignalCellularAlt,
                            contentDescription = null,
                            tint = if (gpsAcquired) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            if (gpsAcquired) "GPS Acquired" else "Acquiring GPS...",
                            color = if (gpsAcquired) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Icon(
                        Icons.Default.OpenInFull,
                        contentDescription = "Expand HUD",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Metrics Row: Time | Split avg | Distance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(elapsedTimeFormatted, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(splitPaceFormatted, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Split avg. (/km)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(distanceKmFormatted, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Distance (km)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}
