package com.ble.broadcaste

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var bleManager: BleManager
    private lateinit var permissionHandler: PermissionHandler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        bleManager = BleManager(this)
        permissionHandler = PermissionHandler(this)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BleApp(
                        bleManager = bleManager,
                        onStartAdvertising = { startAdvertising() },
                        onStopAdvertising = { stopAdvertising() },
                        onStartScanning = { startScanning() },
                        onStopScanning = { stopScanning() }
                    )
                }
            }
        }
    }
    
    private fun startAdvertising() {
        lifecycleScope.launch {
            permissionHandler.checkAndRequestPermissions {
                permissionHandler.enableBluetooth()
                bleManager.startAdvertising()
            }
        }
    }
    
    private fun stopAdvertising() {
        bleManager.stopAdvertising()
    }
    
    private fun startScanning() {
        lifecycleScope.launch {
            permissionHandler.checkAndRequestPermissions {
                permissionHandler.enableBluetooth()
                bleManager.startScanning()
            }
        }
    }
    
    private fun stopScanning() {
        bleManager.stopScanning()
    }
}

data class DiscoveredDevice(
    val device: BluetoothDevice,
    val rssi: Int,
    var isConnected: Boolean = false
)

@Composable
fun BleApp(
    bleManager: BleManager,
    onStartAdvertising: () -> Unit,
    onStopAdvertising: () -> Unit,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit
) {
    var isAdvertising by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var discoveredDevices by remember { mutableStateOf(listOf<DiscoveredDevice>()) }
    
    // Set up BLE manager callbacks
    LaunchedEffect(Unit) {
        bleManager.setDeviceDiscoveryCallback(object : BleManager.DeviceDiscoveryCallback {
            override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
                discoveredDevices = discoveredDevices + DiscoveredDevice(device, rssi)
            }
            
            override fun onDeviceConnected(device: BluetoothDevice) {
                discoveredDevices = discoveredDevices.map { 
                    if (it.device.address == device.address) it.copy(isConnected = true) else it 
                }
            }
            
            override fun onDeviceDisconnected(device: BluetoothDevice) {
                discoveredDevices = discoveredDevices.map { 
                    if (it.device.address == device.address) it.copy(isConnected = false) else it 
                }
            }
        })
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BLE Handshake App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = {
                if (isAdvertising) {
                    onStopAdvertising()
                } else {
                    onStartAdvertising()
                }
                isAdvertising = !isAdvertising
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(if (isAdvertising) "Stop Advertising" else "Start Advertising")
        }
        
        Button(
            onClick = {
                if (isScanning) {
                    onStopScanning()
                } else {
                    onStartScanning()
                }
                isScanning = !isScanning
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(if (isScanning) "Stop Scanning" else "Start Scanning")
        }
        
        if (isScanning) {
            Text(
                text = "Discovered Devices:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(discoveredDevices) { device ->
                    DeviceItem(device)
                }
            }
        }
    }
}

@Composable
fun DeviceItem(device: DiscoveredDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = device.device.name ?: "Unknown Device",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Address: ${device.device.address}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Signal Strength: ${device.rssi} dBm",
                style = MaterialTheme.typography.bodyMedium
            )
            if (device.isConnected) {
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}