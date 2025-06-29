package com.example.myhack

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random
import java.io.InputStream
import java.util.UUID

private const val REQUEST_BLUETOOTH_PERMISSIONS = 100

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private val sensorViews = mutableListOf<View>()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 second

    // Bluetooth related
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)

        // Initialize sensor views
        for (i in 1..18) {
            val viewId = resources.getIdentifier("sensor$i", "id", packageName)
            val sensorView = findViewById<View>(viewId)
            sensorViews.add(sensorView)
        }

        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        } else {
            // Permissions granted — start connection (commented out for now)
            // connectToBluetoothDevice()

            // For now, just start dummy simulation
            startPressureSimulation()
        }
    }

    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // connectToBluetoothDevice()
                startPressureSimulation()
            } else {
                statusText.text = "Bluetooth permissions denied. Running dummy simulation."
                startPressureSimulation()
            }
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun startPressureSimulation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                simulateSensorData()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    private fun simulateSensorData() {
        var abnormalDetected = false

        for (view in sensorViews) {
            val pressure = Random.nextInt(0, 1000)
            when {
                pressure < 100 -> {
                    view.setBackgroundResource(R.drawable.circle_red)
                    abnormalDetected = true
                }
                pressure < 400 -> {
                    view.setBackgroundResource(R.drawable.circle_yellow)
                }
                else -> {
                    view.setBackgroundResource(R.drawable.circle_green)
                }
            }
        }

        statusText.text = if (abnormalDetected) {
            "⚠️ Abnormal Pressure"
        } else {
            "✅ Status: Normal"
        }
    }

    /*
    // Uncomment this method once Bluetooth Arduino part is ready

    private fun connectToBluetoothDevice() {
        // Example: find your Arduino device by name or MAC address
        val deviceName = "YourArduinoBTName"
        val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.firstOrNull { it.name == deviceName }

        if (device == null) {
            runOnUiThread {
                statusText.text = "Bluetooth device not found. Running dummy simulation."
                startPressureSimulation()
            }
            return
        }

        Thread {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Common SPP UUID
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream

                runOnUiThread {
                    statusText.text = "Connected to $deviceName"
                }

                readBluetoothData()

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    statusText.text = "Connection failed. Running dummy simulation."
                    startPressureSimulation()
                }
            }
        }.start()
    }

    private fun readBluetoothData() {
        val buffer = ByteArray(1024)

        while (true) {
            try {
                val bytesRead = inputStream?.read(buffer) ?: 0
                if (bytesRead > 0) {
                    val dataStr = String(buffer, 0, bytesRead)
                    runOnUiThread {
                        updateSensorUI(dataStr)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    statusText.text = "Disconnected. Running dummy simulation."
                    startPressureSimulation()
                }
                break
            }
        }
    }

    private fun updateSensorUI(dataStr: String) {
        val sensorValues = dataStr.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1].toIntOrNull() else null
        }.toMap()

        var abnormal = false
        sensorValues.forEach { (sensorId, value) ->
            val index = sensorId.removePrefix("S").toIntOrNull()?.minus(1)
            if (index != null && index in sensorViews.indices) {
                val view = sensorViews[index]
                when {
                    value < 100 -> {
                        view.setBackgroundResource(R.drawable.circle_red)
                        abnormal = true
                    }
                    value < 400 -> {
                        view.setBackgroundResource(R.drawable.circle_yellow)
                    }
                    else -> {
                        view.setBackgroundResource(R.drawable.circle_green)
                    }
                }
            }
        }

        statusText.text = if (abnormal) "⚠️ Abnormal Pressure" else "✅ Status: Normal"
    }
    */

}
