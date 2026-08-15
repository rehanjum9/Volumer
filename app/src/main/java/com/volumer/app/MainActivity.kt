package com.volumer.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { VolumerScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumerScreen(vm: MainViewModel = viewModel()) {
    val settings by vm.settings.collectAsState()
    var message by remember { mutableStateOf("Ready") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { message = "Permissions updated" }
    val permissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { add(Manifest.permission.BLUETOOTH_SCAN); add(Manifest.permission.BLUETOOTH_CONNECT) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Volumer") }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Smart context-aware media volume", style = MaterialTheme.typography.titleMedium)
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Mode: ${settings.environmentMode}")
                Text("Nearby BLE devices: ${settings.lastNearbyDevices}")
                Text("Public target: ${settings.publicVolumePercent}%")
                Text("Return minimum: ${settings.returnMinimumPercent}%")
                Text("Current media volume: ${vm.currentVolumePercent()}%")
                Text(settings.lastReason)
            }}
            Button(onClick = { permissionLauncher.launch(permissions) }, modifier = Modifier.fillMaxWidth()) { Text("Grant required permissions") }
            Button(onClick = { vm.startMonitor(); message = "Auto monitor started" }, modifier = Modifier.fillMaxWidth()) { Text("Start Auto Monitor") }
            OutlinedButton(onClick = { vm.stopMonitor(); message = "Auto monitor stopped" }, modifier = Modifier.fillMaxWidth()) { Text("Stop Auto Monitor") }
            HorizontalDivider()
            Button(onClick = { vm.simulatePublic(); message = "Public mode test applied" }, modifier = Modifier.fillMaxWidth()) { Text("Test Public Mode (20%)") }
            OutlinedButton(onClick = { vm.simulateQuiet(); message = "Private/quiet test applied" }, modifier = Modifier.fillMaxWidth()) { Text("Test Private Return") }
            HorizontalDivider()
            Button(onClick = { vm.markCurrentLocationPrivate { _, text -> message = text } }, modifier = Modifier.fillMaxWidth()) { Text("Save Current Location as Private") }
            OutlinedButton(onClick = { vm.clearPrivatePlace(); message = "Private place removed" }, modifier = Modifier.fillMaxWidth()) { Text("Clear Private Place") }
            if (!vm.hasBackgroundLocation() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Text("For geofencing while the app is closed, allow Location → Allow all the time in Android app settings.", style = MaterialTheme.typography.bodySmall)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))
            Text("Volumer does not use the microphone. Crowd detection is based on nearby Bluetooth LE density and private-place geofencing.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
