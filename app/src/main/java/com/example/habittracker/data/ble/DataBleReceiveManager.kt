package com.example.habittracker.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.example.habittracker.data.CurrentData
import com.example.habittracker.data.IConnectionState
import com.example.habittracker.data.IDataReceiveManager
import com.example.habittracker.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class DataBleReceiveManager @Inject constructor(
        private val bluetoothAdapter: BluetoothAdapter,
        private val context: Context
) : IDataReceiveManager {

    private val DEVICE_NAME = "HabitTracker"

    override val data: MutableSharedFlow<Resource<CurrentData>>
        get() = MutableSharedFlow()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

    private var gatt: BluetoothGatt? = null

    private var isScanning = false

    private val coroutineScope = CoroutineScope((Dispatchers.Default))

    // every time scanner finds device this cb gets triggered
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // or result.device.address 68 bit address
            if(result.device.name == DEVICE_NAME)
            {
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Connecting to device ..."))
                }
                if (isScanning)
                {
                    result.device.connectGatt(context, false, gattCallback)
                    // or result.device.connectGatt(context,false,gattCallback, BluetoothDevice.TRANSPORT_LE) if ESP32 has BLE AND BL
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }
    }

    private var currentConnectionAttempt = 1
    private val MAX_CONN_ATTEMPTS = 5

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                if (newState == BluetoothProfile.STATE_CONNECTED)
                {
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Discovering Services ..."))
                    }
                    gatt.discoverServices()
                    this@DataBleReceiveManager.gatt = gatt
                } else if(newState == BluetoothProfile.STATE_DISCONNECTED)
                {
                    coroutineScope.launch {
                        data.emit(Resource.Success(data = CurrentData(0, IConnectionState.Disconnected)))
                    }
                    gatt.close()
                }
            } else
            {
                gatt.close()
                currentConnectionAttempt += 1
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Attempting to connect $currentConnectionAttempt/$MAX_CONN_ATTEMPTS"))
                }
                if (currentConnectionAttempt <= MAX_CONN_ATTEMPTS) {
                    startReceiving()
                } else {
                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not connect to ble device"))
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status : Int) {
            with(gatt) {
                printGattTable()
            }
        }

    }

    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message = "Scanning Ble devices..."))
        }
        isScanning = true
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    override fun reconnect() {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun closeConnection() {
        TODO("Not yet implemented")
    }


}