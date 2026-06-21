package com.example.sololeveling90days.ui.jogging

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.data.JoggingSession
import com.example.sololeveling90days.data.LatLngPoint
import com.example.sololeveling90days.theme.DarkBg
import com.example.sololeveling90days.theme.DisciplineNavy
import com.example.sololeveling90days.theme.AppleBlue
import com.example.sololeveling90days.theme.HardRed
import com.example.sololeveling90days.theme.TextPrimary
import com.example.sololeveling90days.theme.TextSecondary
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoggingTrackerScreen(
    repository: AppRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessions by repository.joggingSessions.collectAsStateWithLifecycle(emptyList())

    // Design Tokens (Apple HIG style)
    val systemBackground = DarkBg
    val secondaryBackground = DisciplineNavy
    val appleBlue = AppleBlue
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary

    // State Variables
    var isTracking by remember { mutableStateOf(false) }
    var durationSeconds by remember { mutableLongStateOf(0L) }
    var distanceKm by remember { mutableFloatStateOf(0f) }
    val routePoints = remember { mutableStateListOf<LatLng>() }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

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

    // Location update logic during tracking
    DisposableEffect(isTracking, locationPermissionGranted) {
        var locationCallback: LocationCallback? = null
        
        if (isTracking && locationPermissionGranted) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
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
                        distanceKm += results[0] / 1000f
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

    // UI Structure
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(systemBackground)
    ) {
        // Large Title Header
        Text(
            text = "Jogging",
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        // Google Map Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(secondaryBackground),
            contentAlignment = Alignment.Center
        ) {
            if (locationPermissionGranted) {
                if (isDelayActive) {
                    CircularProgressIndicator(
                        color = appleBlue,
                        strokeWidth = 3.dp
                    )
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
                        polylineColor = appleBlue,
                        onMapReady = {
                            mapOpacity = 1f
                        }
                    )

                    if (mapOpacity == 0f) {
                        CircularProgressIndicator(
                            color = appleBlue,
                            strokeWidth = 3.dp
                        )
                    }
                }
            } else {
                // Request location permissions display state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Location Permission Required",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
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
                        colors = ButtonDefaults.buttonColors(containerColor = appleBlue)
                    ) {
                        Text("Grant Permission", color = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Control Panel Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = secondaryBackground)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Distance
                    Column {
                        Text("Distance", color = textSecondary, fontSize = 12.sp)
                        Text(
                            text = String.format("%.2f km", distanceKm),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    // Duration
                    Column {
                        Text("Time", color = textSecondary, fontSize = 12.sp)
                        Text(
                            text = formatDuration(durationSeconds),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    // Pace
                    Column {
                        Text("Pace", color = textSecondary, fontSize = 12.sp)
                        Text(
                            text = formatPace(distanceKm, durationSeconds),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Start/Stop buttons
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
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = appleBlue)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Jogging", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            isTracking = false
                            // Save session
                            if (distanceKm > 0.05f) {
                                val session = JoggingSession(
                                    id = UUID.randomUUID().toString(),
                                    date = LocalDate.now().toString(),
                                    distanceKm = distanceKm,
                                    durationSeconds = durationSeconds,
                                    routePoints = routePoints.map { LatLngPoint(it.latitude, it.longitude) }
                                )
                                scope.launch {
                                    repository.addJoggingSession(session)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HardRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop run", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // History Section
        Text(
            text = "Recent runs",
            color = textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            items(sessions.reversed()) { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(secondaryBackground)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = session.date,
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = formatDuration(session.durationSeconds),
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = String.format("%.2f km", session.distanceKm),
                        color = appleBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
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
    
    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { view ->
        view.getMapAsync { googleMap ->
            onMapReady(view)
            googleMap.clear()
            
            // Check self permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.isMyLocationEnabled = true
            }

            // Draw route path
            if (points.isNotEmpty()) {
                val polylineOptions = PolylineOptions()
                    .addAll(points)
                    .color(polylineColor.toArgb())
                    .width(12f)
                googleMap.addPolyline(polylineOptions)
            }
            
            // Focus on current location
            currentLocation?.let {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

private fun formatPace(distanceKm: Float, seconds: Long): String {
    if (distanceKm <= 0.01f) return "--:--"
    val totalMins = seconds.toFloat() / 60f
    val paceFloat = totalMins / distanceKm
    val paceMins = paceFloat.toInt()
    val paceSecs = ((paceFloat - paceMins) * 60).toInt()
    return String.format("%d:%02d/km", paceMins, paceSecs)
}
