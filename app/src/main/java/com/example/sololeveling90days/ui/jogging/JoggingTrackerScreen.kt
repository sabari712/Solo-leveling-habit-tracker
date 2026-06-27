package com.example.sololeveling90days.ui.jogging

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.data.ActivityType
import com.example.sololeveling90days.data.JoggingSession
import com.example.sololeveling90days.data.LatLngPoint
import com.example.sololeveling90days.data.UserProfile
import com.example.sololeveling90days.theme.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

// Activity-specific accent colors
private val ActivityColors = mapOf(
    ActivityType.WALKING to Color(0xFF4CAF50),
    ActivityType.JOGGING to Color(0xFF2196F3),
    ActivityType.RUNNING to Color(0xFFFF9800),
    ActivityType.SPRINTING to Color(0xFFFF5722),
    ActivityType.CYCLING to Color(0xFF9C27B0),
    ActivityType.HIKING to Color(0xFF795548)
)

// GPS noise filter constants
private const val MIN_DISTANCE_METERS = 5f     // Ignore updates closer than 5m
private const val MIN_ACCURACY_METERS = 25f    // Ignore updates with accuracy > 25m

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoggingTrackerScreen(
    repository: AppRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessions by repository.joggingSessions.collectAsStateWithLifecycle(emptyList())
    val profile by repository.userProfile.collectAsStateWithLifecycle(UserProfile())

    // State Variables
    var selectedActivity by remember { mutableStateOf(ActivityType.JOGGING) }
    var isTracking by remember { mutableStateOf(false) }
    var durationSeconds by remember { mutableLongStateOf(0L) }
    var distanceKm by remember { mutableFloatStateOf(0f) }
    val routePoints = remember { mutableStateListOf<LatLng>() }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var showXpToast by remember { mutableStateOf(false) }
    var lastXpEarned by remember { mutableStateOf(0) }

    // Fused Location Provider setup
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Location permission state
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // Live calorie calculation
    val liveCalories = remember(selectedActivity, durationSeconds) {
        val weightKg = if (profile.weightKg > 0f) profile.weightKg else 70f
        selectedActivity.caloriesBurned(weightKg, durationSeconds)
    }

    // Live XP calculation
    val liveXp = remember(selectedActivity, distanceKm) {
        selectedActivity.xpEarned(distanceKm)
    }

    // Activity accent color
    val accentColor = ActivityColors[selectedActivity] ?: AppleBlue

    // Timer logic
    LaunchedEffect(isTracking) {
        if (isTracking) {
            while (isTracking) {
                delay(1000L)
                durationSeconds++
            }
        }
    }

    // Map initialization delay to prevent transition stutter
    var isDelayActive by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(600L)
        isDelayActive = false
    }

    // Location update logic during tracking — WITH GPS NOISE FILTER
    DisposableEffect(isTracking, locationPermissionGranted) {
        var locationCallback: LocationCallback? = null

        if (isTracking && locationPermissionGranted) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(3f) // Hardware-level minimum filter
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return

                    // GPS NOISE FILTER: reject inaccurate fixes
                    if (location.hasAccuracy() && location.accuracy > MIN_ACCURACY_METERS) return

                    val newLatLng = LatLng(location.latitude, location.longitude)
                    currentLocation = newLatLng

                    if (routePoints.isNotEmpty()) {
                        val lastPoint = routePoints.last()
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            lastPoint.latitude, lastPoint.longitude,
                            newLatLng.latitude, newLatLng.longitude,
                            results
                        )
                        val distanceMeters = results[0]

                        // GPS NOISE FILTER: reject tiny movements (jitter)
                        if (distanceMeters < MIN_DISTANCE_METERS) return

                        distanceKm += distanceMeters / 1000f
                    }
                    routePoints.add(newLatLng)
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                isTracking = false
            }
        }

        onDispose {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
        }
    }

    // Initialize current location once
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    loc?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {}
        }
    }

    // XP Toast auto-dismiss
    LaunchedEffect(showXpToast) {
        if (showXpToast) {
            delay(3000)
            showXpToast = false
        }
    }

    // UI Structure
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
        ) {
            // Title Header
            Text(
                text = "Activity Tracker",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = TextPrimary,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
            )

            // Activity Type Selector — horizontal chip row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityType.entries.forEach { activity ->
                    val isSelected = activity == selectedActivity
                    val chipColor = ActivityColors[activity] ?: AppleBlue

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = !isTracking) {
                                selectedActivity = activity
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) chipColor.copy(alpha = 0.2f) else DisciplineNavy,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(1.5.dp, chipColor)
                        } else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(activity.emoji, fontSize = 16.sp)
                            Text(
                                activity.label,
                                color = if (isSelected) chipColor else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Google Map Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DisciplineNavy),
                contentAlignment = Alignment.Center
            ) {
                if (locationPermissionGranted) {
                    if (isDelayActive) {
                        CircularProgressIndicator(color = accentColor, strokeWidth = 3.dp)
                    } else {
                        var mapOpacity by remember { mutableStateOf(0f) }
                        val alphaAnimated by animateFloatAsState(
                            targetValue = mapOpacity,
                            animationSpec = tween(durationMillis = 400),
                            label = "MapFadeIn"
                        )

                        GoogleMapView(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = alphaAnimated },
                            points = routePoints,
                            currentLocation = currentLocation,
                            polylineColor = accentColor,
                            onMapReady = { mapOpacity = 1f }
                        )

                        if (mapOpacity == 0f) {
                            CircularProgressIndicator(color = accentColor, strokeWidth = 3.dp)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Location Permission Required", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Grant Permission", color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Live Stats Panel — 4 metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Distance
                    StatItem(
                        label = "Distance",
                        value = String.format("%.2f", distanceKm),
                        unit = "km",
                        color = accentColor
                    )
                    // Duration
                    StatItem(
                        label = "Time",
                        value = formatDuration(durationSeconds),
                        unit = "",
                        color = TextPrimary
                    )
                    // Pace or Speed (based on activity type)
                    if (selectedActivity.showSpeed) {
                        StatItem(
                            label = "Speed",
                            value = formatSpeed(distanceKm, durationSeconds),
                            unit = "km/h",
                            color = accentColor
                        )
                    } else {
                        StatItem(
                            label = "Pace",
                            value = formatPace(distanceKm, durationSeconds),
                            unit = "/km",
                            color = accentColor
                        )
                    }
                    // Calories
                    StatItem(
                        label = "Cal",
                        value = "$liveCalories",
                        unit = "kcal",
                        color = Color(0xFFFF6B6B)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // XP Preview + Start/Stop Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // XP preview row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("\u26A1", fontSize = 16.sp)
                            Text(
                                "XP Reward",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            "+$liveXp XP",
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Activity rate info
                    Text(
                        "${selectedActivity.xpPerKm} XP/km \u00B7 MET ${selectedActivity.met}",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Start/Stop button
                    if (!isTracking) {
                        Button(
                            onClick = {
                                if (locationPermissionGranted) {
                                    isTracking = true
                                    distanceKm = 0f
                                    durationSeconds = 0L
                                    routePoints.clear()
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Start ${selectedActivity.label}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isTracking = false
                                // Save session with full stats
                                if (distanceKm > 0.01f || durationSeconds > 30) {
                                    val weightKg = if (profile.weightKg > 0f) profile.weightKg else 70f
                                    val calories = selectedActivity.caloriesBurned(weightKg, durationSeconds)
                                    val xp = selectedActivity.xpEarned(distanceKm)
                                    val avgPace = if (distanceKm > 0.01f) {
                                        (durationSeconds / distanceKm).toInt()
                                    } else 0

                                    val session = JoggingSession(
                                        id = UUID.randomUUID().toString(),
                                        date = LocalDate.now().toString(),
                                        distanceKm = distanceKm,
                                        durationSeconds = durationSeconds,
                                        routePoints = routePoints.map { LatLngPoint(it.latitude, it.longitude) },
                                        activityType = selectedActivity,
                                        caloriesBurned = calories,
                                        avgPaceSecondsPerKm = avgPace,
                                        xpEarned = xp
                                    )
                                    lastXpEarned = xp
                                    showXpToast = xp > 0
                                    scope.launch {
                                        repository.addJoggingSession(session)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HardRed)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Stop & Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // History Section
            Text(
                text = "Recent Activities",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sessions.reversed()) { session ->
                    val sessionColor = ActivityColors[session.activityType] ?: AppleBlue
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DisciplineNavy)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Activity emoji badge
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(sessionColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(session.activityType.emoji, fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(12.dp))

                            // Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    session.activityType.label,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "${session.date} \u00B7 ${formatDuration(session.durationSeconds)}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            // Stats
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    String.format("%.2f km", session.distanceKm),
                                    color = sessionColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (session.caloriesBurned > 0) {
                                        Text(
                                            "${session.caloriesBurned} kcal",
                                            color = Color(0xFFFF6B6B),
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (session.xpEarned > 0) {
                                        Text(
                                            "+${session.xpEarned} XP",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // XP Toast overlay
        AnimatedVisibility(
            visible = showXpToast,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("\u26A1", fontSize = 22.sp)
                    Text(
                        "+$lastXpEarned XP Earned!",
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(
            value,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (unit.isNotEmpty()) {
            Text(unit, color = TextSecondary.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
fun GoogleMapView(
    modifier: Modifier,
    points: List<LatLng>,
    currentLocation: LatLng?,
    polylineColor: Color,
    onMapReady: (MapView) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    // Manage MapView lifecycle
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                androidx.lifecycle.Lifecycle.Event.ON_START -> mapView.onStart()
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> mapView.onStop()
                androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    // Initialize map once
    LaunchedEffect(mapView) {
        mapView.getMapAsync { googleMap ->
            googleMapRef = googleMap
            onMapReady(mapView)

            // Enable my location if permitted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap.isMyLocationEnabled = true
                } catch (_: SecurityException) {}
            }
        }
    }

    // Update map incrementally when points/location change
    LaunchedEffect(points.size, currentLocation) {
        val gMap = googleMapRef ?: return@LaunchedEffect

        // Redraw polyline
        gMap.clear()
        if (points.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .color(polylineColor.toArgb())
                .width(14f)
            gMap.addPolyline(polylineOptions)
        }

        // Focus camera on current location
        currentLocation?.let {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

private fun formatPace(distanceKm: Float, seconds: Long): String {
    if (distanceKm <= 0.01f) return "--:--"
    val totalMins = seconds.toFloat() / 60f
    val paceFloat = totalMins / distanceKm
    val paceMins = paceFloat.toInt()
    val paceSecs = ((paceFloat - paceMins) * 60).toInt()
    return String.format("%d:%02d", paceMins, paceSecs)
}

private fun formatSpeed(distanceKm: Float, seconds: Long): String {
    if (seconds <= 0) return "0.0"
    val hours = seconds.toFloat() / 3600f
    val speed = distanceKm / hours
    return String.format("%.1f", speed)
}
