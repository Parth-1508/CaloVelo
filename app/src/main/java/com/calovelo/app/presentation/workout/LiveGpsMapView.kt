package com.calovelo.app.presentation.workout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.Color as AndroidColor
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
import androidx.compose.ui.viewinterop.AndroidView
import com.calovelo.app.domain.model.LatLng
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/** High-DPI, ultra-sharp native dark mode map tiles (CartoDB DarkMatter) */
private val CartoDbDarkMatter = object : OnlineTileSourceBase(
    "CartoDB-DarkMatter",
    0, 20, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/dark_all/",
        "https://b.basemaps.cartocdn.com/dark_all/",
        "https://c.basemaps.cartocdn.com/dark_all/",
        "https://d.basemaps.cartocdn.com/dark_all/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
    }
}

/** Generates a crisp, high-resolution Strava-style location puck drawable */
private fun createStravaLocationPuck(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (48 * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val center = sizePx / 2f

    // Outer pulsing aura
    val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#4DFC4C02") // Strava Orange 30% alpha
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, center, auraPaint)

    // White border ring
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, center * 0.45f, borderPaint)

    // Inner Strava Orange core
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#FC4C02")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, center * 0.32f, innerPaint)

    return BitmapDrawable(context.resources, bitmap)
}

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

    // Set user agent for OpenStreetMap tile server
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val defaultGeoPoint = remember { GeoPoint(18.5204, 73.8567) } // Pune default fallback
    val currentGeoPoint = remember(currentLocation) {
        currentLocation?.let { GeoPoint(it.latitude, it.longitude) } ?: defaultGeoPoint
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val puckIcon = remember(context) { createStravaLocationPuck(context) }

    // Re-center map when location updates
    LaunchedEffect(currentGeoPoint) {
        mapViewRef?.let { map ->
            if (currentLocation != null) {
                map.controller.animateTo(currentGeoPoint)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // High-DPI Sharp Dark Mode OpenStreetMap View
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(CartoDbDarkMatter)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    controller.setZoom(17.5)
                    controller.setCenter(currentGeoPoint)

                    // Add live GPS location overlay
                    val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                        enableMyLocation()
                        enableFollowLocation()
                        setPersonIcon(puckIcon.bitmap)
                    }
                    overlays.add(myLocationOverlay)
                    locationOverlayRef = myLocationOverlay

                    mapViewRef = this
                }
            },
            update = { map ->
                // Preserve location overlay, update route polylines & location puck
                val locationOverlay = locationOverlayRef
                map.overlays.clear()
                if (locationOverlay != null) {
                    map.overlays.add(locationOverlay)
                }

                // Draw glowing cyan route polyline
                if (routePoints.size > 1) {
                    val geoPoints = routePoints.map { GeoPoint(it.latitude, it.longitude) }
                    val polyline = Polyline().apply {
                        setPoints(geoPoints)
                        outlinePaint.color = AndroidColor.parseColor("#00D2FF")
                        outlinePaint.strokeWidth = 14f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        outlinePaint.strokeJoin = Paint.Join.ROUND
                    }
                    map.overlays.add(polyline)
                }

                // Draw custom crisp location puck marker if available
                if (currentLocation != null) {
                    val userMarker = Marker(map).apply {
                        position = currentGeoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = puckIcon
                        title = "Current Location"
                    }
                    map.overlays.add(userMarker)
                }

                map.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

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
                    .background(Color.Black.copy(alpha = 0.75f))
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
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFFFC4C02).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
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
                            "CaloVelo OpenMap Tracker",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            "100% Free OpenStreetMap GPS",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Floating Map Controls on Right (Re-center location)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable {
                        locationOverlayRef?.enableFollowLocation()
                        mapViewRef?.let { map ->
                            currentLocation?.let { loc ->
                                map.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
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
