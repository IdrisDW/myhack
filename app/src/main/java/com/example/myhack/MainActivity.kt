package com.example.myhack

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myhack.uiApp.FootHeatMapView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var footHeatMapView: FootHeatMapView
    private lateinit var statusText: TextView
    private lateinit var modeToggle: Switch

    private val handler = Handler(Looper.getMainLooper())
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var isRunning = false

    // Will be updated by toggle listener
    private var useSimulation = true

    // Simulation update runnable
    private val updateTask = object : Runnable {
        override fun run() {
            val simulatedData = generateSimulatedPressureData()
            footHeatMapView.updatePressures(simulatedData)
            statusText.text = if (isPressureNormal(simulatedData)) "✅ Right foot normal" else "⚠️ Right foot abnormal"
            appendDataToCsv(simulatedData)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        setContentView(R.layout.activity_main)

        footHeatMapView = findViewById(R.id.heatmapView)
        statusText = findViewById(R.id.statusText)
        modeToggle = findViewById(R.id.modeToggle)

        useSimulation = modeToggle.isChecked

        // Start initial mode
        if (useSimulation) {
            startSimulation()
            statusText.text = "🧪 Simulation mode"
        } else {
            startBluetooth()
            statusText.text = "📶 Connecting Bluetooth..."
        }

        // Toggle listener
        modeToggle.setOnCheckedChangeListener { _, isChecked ->
            useSimulation = isChecked
            if (useSimulation) {
                stopBluetooth()
                startSimulation()
                statusText.text = "🧪 Simulation mode"
            } else {
                stopSimulation()
                startBluetooth()
                statusText.text = "📶 Connecting Bluetooth..."
            }
        }
    }

    // ---------------- Simulation ----------------
    private fun startSimulation() {
        handler.post(updateTask)
    }

    private fun stopSimulation() {
        handler.removeCallbacks(updateTask)
    }

    private fun generateSimulatedPressureData(): List<Float> {
        return List(9) {
            (Random.nextFloat() * 100).roundToInt() / 100f
        }
    }

    // ---------------- Bluetooth ------------------
    private fun startBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                100
            )
            return
        }

        thread {
            val deviceName = "raspberry" // Change this to your Pi's Bluetooth name
            val device = bluetoothAdapter?.bondedDevices?.firstOrNull {
                it.name == deviceName
            }

            if (device == null) {
                runOnUiThread {
                    statusText.text = "❌ Device not paired"
                }
                return@thread
            }

            try {
                val uuid = device.uuids?.firstOrNull()?.uuid
                    ?: UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

                socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                socket?.connect()

                runOnUiThread {
                    statusText.text = "🔗 Connected to ${device.name}"
                }

                isRunning = true
                val reader = BufferedReader(InputStreamReader(socket!!.inputStream))

                while (isRunning) {
                    val line = reader.readLine() ?: continue
                    val values = line.trim().split(";").mapNotNull {
                        it.toFloatOrNull()?.div(1023f)
                    }
                    if (values.size >= 9) {
                        runOnUiThread {
                            footHeatMapView.updatePressures(values.take(9))
                            statusText.text = if (isPressureNormal(values)) "✅ Right foot normal" else "⚠️ Right foot abnormal"
                        }
                        appendDataToCsv(values.take(9))
                    }
                }

            } catch (e: Exception) {
                Log.e("BT", "Connection error", e)
                runOnUiThread {
                    statusText.text = "❌ Bluetooth error: ${e.message}"
                }
            }
        }
    }

    private fun stopBluetooth() {
        isRunning = false
        socket?.close()
    }

    // ---------------- CSV Logging ----------------
    private fun appendDataToCsv(data: List<Float>) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val line = "$timestamp,${data.joinToString(",")}\n"
            val file = File(filesDir, "pressure_data.csv")
            val writer = FileWriter(file, true)
            writer.append(line)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            Log.e("CSV", "Error writing CSV", e)
        }
    }

    private fun isPressureNormal(data: List<Float>): Boolean {
        return data.none { it > 0.85f || it < 0.1f }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSimulation()
        stopBluetooth()
    }
}
