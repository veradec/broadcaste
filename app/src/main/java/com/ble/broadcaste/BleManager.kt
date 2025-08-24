package com.ble.broadcaste

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class BleManager(private val context: Context) {
    private val TAG = "BleManager"
    private val SERVICE_UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID = UUID.fromString("00001235-0000-1000-8000-00805f9b34fb")
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var advertisingSet: AdvertisingSet? = null
    private var gattServer: BluetoothGattServer? = null
    
    private var isAdvertising = false
    private var isScanning = false
    
    // Callback interface for device discovery
    interface DeviceDiscoveryCallback {
        fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int)
        fun onDeviceConnected(device: BluetoothDevice)
        fun onDeviceDisconnected(device: BluetoothDevice)
    }
    
    private var discoveryCallback: DeviceDiscoveryCallback? = null
    
    fun setDeviceDiscoveryCallback(callback: DeviceDiscoveryCallback) {
        discoveryCallback = callback
    }
    
    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }
    
    fun startAdvertising() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth adapter not available or not enabled")
            return
        }
        
        if (isAdvertising) {
            Log.d(TAG, "Already advertising")
            return
        }
        
        val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        if (advertiser == null) {
            Log.e(TAG, "Bluetooth LE Advertiser not available")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setConnectable(true)
            .setTimeout(0) // Advertise indefinitely
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()
            
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
            
        val scanResponse = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .build()
            
        advertiser.startAdvertising(settings, data, scanResponse, advertisingCallback)
        isAdvertising = true
    }
    
    fun stopAdvertising() {
        if (!isAdvertising) return
        
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertisingCallback)
        isAdvertising = false
    }
    
    fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth adapter not available or not enabled")
            return
        }
        
        if (isScanning) {
            Log.d(TAG, "Already scanning")
            return
        }
        
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
        )
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
            
        bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        isScanning = true
    }
    
    fun stopScanning() {
        if (!isScanning) return
        
        bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
    }
    
    private val advertisingCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Advertising started successfully")
        }
        
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Failed to start advertising: $errorCode")
            isAdvertising = false
        }
    }
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val rssi = result.rssi
            Log.d(TAG, "Found device: ${device.address} (RSSI: $rssi)")
            
            discoveryCallback?.onDeviceDiscovered(device, rssi)
            
            // Connect to the device
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                device.connectGatt(context, false, gattCallback)
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error: $errorCode")
        }
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server")
                    discoveryCallback?.onDeviceConnected(gatt.device)
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    discoveryCallback?.onDeviceDisconnected(gatt.device)
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                // Handle service discovery
            }
        }
    }
} 