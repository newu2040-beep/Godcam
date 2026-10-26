package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppDestination
import com.example.ui.GodcamViewModel
import com.example.ui.components.GodcamHeroSplashOverlay
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.FiltersScreen
import com.example.ui.screens.GalleryAndExportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GodcamForestActive
import com.example.ui.theme.GodcamMintAccent
import com.example.ui.theme.GodcamTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GodcamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by viewModel.preferencesState.collectAsStateWithLifecycle()

            GodcamTheme(themeOption = preferences.theme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GodcamAppRoot(viewModel = viewModel)
                }
            }
        }
    }
}

private fun requiredRuntimePermissions(): Array<String> {
    return buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()
}

private fun syncPermissionState(context: Context, viewModel: GodcamViewModel): Boolean {
    val camGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    val micGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    viewModel.onPermissionsUpdated(
        cameraGranted = camGranted,
        micGranted = micGranted,
        notificationsGranted = notifGranted
    )
    return camGranted && micGranted && notifGranted
}

@Composable
fun GodcamAppRoot(viewModel: GodcamViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
    val favoritePresetIds by viewModel.favoritePresetIds.collectAsStateWithLifecycle()
    val preferences by viewModel.preferencesState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        syncPermissionState(context, viewModel)
    }

    val requestPermissionsAction = {
        permissionLauncher.launch(requiredRuntimePermissions())
    }

    // Automatically check and request all required permissions (Camera, Microphone, Notifications, Gallery Save) on launch
    LaunchedEffect(Unit) {
        val allGranted = syncPermissionState(context, viewModel)
        if (!allGranted) {
            permissionLauncher.launch(requiredRuntimePermissions())
        }
    }

    // Re-sync permission state whenever user returns from system settings or permission dialog
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                syncPermissionState(context, viewModel)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (uiState.showPermissionsDialog) {
        PermissionsAndGalleryAccessDialog(
            cameraGranted = uiState.cameraPermissionGranted,
            micGranted = uiState.micPermissionGranted,
            notificationsGranted = uiState.notificationsPermissionGranted,
            onRequestPermissions = {
                viewModel.setShowPermissionsDialog(false)
                requestPermissionsAction()
            },
            onOpenSystemSettings = {
                viewModel.setShowPermissionsDialog(false)
                viewModel.openSystemAppSettings(context)
            },
            onDismiss = { viewModel.setShowPermissionsDialog(false) }
        )
    }

    Crossfade(
        targetState = uiState.destination,
        label = "godcam_destination_transition"
    ) { destination ->
        when (destination) {
            AppDestination.SPLASH_HERO -> {
                GodcamHeroSplashOverlay(
                    onEnterCamera = { viewModel.navigateTo(AppDestination.CAMERA) },
                    onOpenGallery = { viewModel.navigateTo(AppDestination.GALLERY) }
                )
            }

            AppDestination.CAMERA -> {
                CameraScreen(
                    uiState = uiState,
                    recentMedia = mediaItems.firstOrNull(),
                    viewModel = viewModel,
                    onRequestPermissions = requestPermissionsAction
                )
            }

            AppDestination.FILTERS -> {
                FiltersScreen(
                    uiState = uiState,
                    favoritePresetIds = favoritePresetIds,
                    viewModel = viewModel
                )
            }

            AppDestination.GALLERY -> {
                GalleryAndExportScreen(
                    uiState = uiState,
                    mediaItems = mediaItems,
                    viewModel = viewModel
                )
            }

            AppDestination.SETTINGS -> {
                SettingsScreen(
                    uiState = uiState,
                    preferences = preferences,
                    viewModel = viewModel,
                    onRequestPermissions = requestPermissionsAction
                )
            }
        }
    }
}

@Composable
private fun PermissionsAndGalleryAccessDialog(
    cameraGranted: Boolean,
    micGranted: Boolean,
    notificationsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permissions & Gallery Auto-Save",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Allow GODCAM access so every photo and video you capture automatically saves into your Gallery with status notifications.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                PermissionRowItem(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Camera Sensor",
                    subtitle = "Live optical viewfinder, Pro Photo & 4K Video",
                    granted = cameraGranted
                )
                PermissionRowItem(
                    icon = Icons.Outlined.Mic,
                    title = "Microphone Audio",
                    subtitle = "Stereo audio for Video & Pro Video recording",
                    granted = micGranted
                )
                PermissionRowItem(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Notifications",
                    subtitle = "Instant alerts when photos/videos save to Gallery & Export completes",
                    granted = notificationsGranted
                )
                PermissionRowItem(
                    icon = Icons.Outlined.Collections,
                    title = "Gallery & MediaStore Auto-Save",
                    subtitle = "Automatic saving to DCIM/GODCAM & In-App Gallery",
                    granted = true
                )
            }
        },
        confirmButton = {
            Surface(
                onClick = onRequestPermissions,
                shape = RoundedCornerShape(50),
                color = GodcamForestActive,
                contentColor = Color.White,
                modifier = Modifier.testTag("dialog_grant_all_permissions")
            ) {
                Text(
                    text = "Allow All Permissions",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSystemSettings) {
                    Text("App Settings")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun PermissionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    granted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    if (granted) GodcamMintAccent.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (granted) GodcamForestActive else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        if (granted) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Granted",
                tint = GodcamForestActive,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = "Allow",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
