package com.example.myhack

import android.Manifest
import android.bluetooth.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myhack.uiApp.FootHeatMapView
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var footHeatMapView: FootHeatMapView
    private lateinit var statusText: TextView
    private lateinit var toggleSimButton: Button

    private var isSimulating = true

    // Bluetooth related
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private val handler = Handler(Looper.getMainLooper())

    // Use your Raspberry Pi Bluetooth device MAC address here
    private val raspberryPiMac = "D8:3A:DD:D8:46:90"

    // UUID for SPP (Serial Port Profile)
    private val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothReadBuffer = StringBuilder()

    // --- Permissions you need to request ---
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    // Register the permission launcher
    private val requestBluetoothPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            connectToRaspberryPi()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        footHeatMapView = findViewById(R.id.heatmapView)
        statusText = findViewById(R.id.statusText)
        toggleSimButton = findViewById(R.id.toggleSimButton)

        toggleSimButton.setOnClickListener {
            isSimulating = !isSimulating
            if (isSimulating) {
                statusText.text = "Simulation mode ON"
                handler.removeCallbacks(bluetoothConnectRunnable)
                handler.removeCallbacks(readRunnable)
                handler.post(simulationRunnable)
            } else {
                statusText.text = "Bluetooth mode ON"
                handler.removeCallbacks(simulationRunnable)
                checkPermissionsAndConnectBluetooth()
            }
        }

        // Start in simulation mode
        statusText.text = "Simulation mode ON"
        handler.post(simulationRunnable)
    }

    private fun checkPermissionsAndConnectBluetooth() {
        val missingPermissions = bluetoothPermissions.any {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions) {
            requestBluetoothPermissionsLauncher.launch(bluetoothPermissions)
        } else {
            connectToRaspberryPi()
        }
    }

    // Simulation code
    private val simulationRunnable = object : Runnable {
        override fun run() {
            val simulatedData = generateSimulatedPressureData()
            footHeatMapView.updatePressures(simulatedData)
            statusText.text = if (isPressureNormal(simulatedData)) "✅ Normal gait (Simulated)" else "⚠️ Abnormal pressure (Simulated)"
            handler.postDelayed(this, 1000)
        }
    }

    private fun generateSimulatedPressureData(): List<Float> {
        return List(9) { (Math.random() * 1.0).toFloat() }
    }

    private fun isPressureNormal(data: List<Float>): Boolean {
        return data.none { it > 0.85f || it < 0.1f }
    }

    // Bluetooth connection code
    private fun connectToRaspberryPi() {
        val device = bluetoothAdapter?.getRemoteDevice(raspberryPiMac)
        if (device == null) {
            Toast.makeText(this, "Raspberry Pi device not found", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(sppUUID)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream
                handler.post {
                    statusText.text = "Connected to Raspberry Pi"
                }
                handler.post(readRunnable)
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to Raspberry Pi", e)
                handler.post {
                    statusText.text = "Connection failed: ${e.message}"
                    isSimulating = true
                    handler.post(simulationRunnable)
                }
            }
        }.start()
    }

    private val readRunnable = object : Runnable {
        override fun run() {
            readBluetoothData()
            handler.postDelayed(this, 500) // check every 0.5 sec
        }
    }

    private fun readBluetoothData() {
        try {
            val available = inputStream?.available() ?: 0
            if (available > 0) {
                val buffer = ByteArray(available)
                inputStream?.read(buffer)
                val received = String(buffer)
                bluetoothReadBuffer.append(received)

                // Check if we have a full data string terminated by newline
                val fullData = bluetoothReadBuffer.toString()
                if (fullData.contains("\n")) {
                    val lines = fullData.split("\n")
                    // Process all full lines except last (possibly incomplete)
                    for (i in 0 until lines.size - 1) {
                        processSensorDataLine(lines[i].trim())
                    }
                    // Keep last partial line in buffer
                    bluetoothReadBuffer.clear()
                    bluetoothReadBuffer.append(lines.last())
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading Bluetooth data", e)
            handler.post {
                statusText.text = "Disconnected"
                isSimulating = true
                handler.post(simulationRunnable)
            }
        }
    }

    private fun processSensorDataLine(line: String) {
        val parts = line.split(";")
        if (parts.size >= 9) {
            try {
                val pressures = parts.take(9).map {
                    val raw = it.toFloatOrNull() ?: 0f
                    (raw / 1023f).coerceIn(0f, 1f)
                }
                runOnUiThread {
                    footHeatMapView.updatePressures(pressures)
                    statusText.text = if (isPressureNormal(pressures)) "✅ Normal gait" else "⚠️ Abnormal pressure"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse sensor data", e)
            }
        } else {
            Log.w(TAG, "Incomplete sensor data: $line")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothSocket?.close()
        handler.removeCallbacks(readRunnable)
        handler.removeCallbacks(simulationRunnable)
        handler.removeCallbacks(bluetoothConnectRunnable)
    }

    private val bluetoothConnectRunnable = Runnable {
        connectToRaspberryPi()
    }
}
