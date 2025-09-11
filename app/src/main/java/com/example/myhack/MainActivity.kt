package com.example.myhack

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.myhack.uiApp.FootHeatMapView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var footHeatMapView: FootHeatMapView
    private lateinit var statusText: TextView
    private lateinit var toggleSimButton: Button
    private lateinit var btnToggleView: Button
    private lateinit var btnExportCsv: Button

    private lateinit var chart: LineChart
    private lateinit var dataSet: LineDataSet
    private lateinit var lineData: LineData
    private var startTime = System.currentTimeMillis()
    private val selectedSensorIndex = 0

    private val handler = Handler(Looper.getMainLooper())
    private var isSimulating = true

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var reader: BufferedReader? = null
    private val bluetoothReadBuffer = StringBuilder()
    private var hasLoggedReadStart = false

    private val raspberryPiMac = "D8:3A:DD:D8:46:90"
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val csvFile by lazy {
        File(getExternalFilesDir(null), "sensor_data.csv").apply {
            if (!exists()) appendText("timestamp,s1,s2,s3,s4,s5,s6,s7,s8,s9\n")
        }
    }
    private val readRunnable = object : Runnable {
        override fun run() {
            if (!hasLoggedReadStart) {
                Log.d(TAG, "Read loop started")
                hasLoggedReadStart = true
            }

            val socket = bluetoothSocket
            val stream = inputStream

            if (socket == null || !socket.isConnected || stream == null) {
                Log.w(TAG, "Socket/stream not ready — skipping read")
                return
            }

            try {
                if (stream.available() > 0) {
                    val buffer = ByteArray(1024)
                    val bytesRead = stream.read(buffer)

                    if (bytesRead > 0) {
                        val received = String(buffer, 0, bytesRead, Charsets.UTF_8)
                        bluetoothReadBuffer.append(received)

                        val fullData = bluetoothReadBuffer.toString()
                        if (fullData.contains("\n")) {
                            val lines = fullData.split("\n")
                            for (i in 0 until lines.size - 1) {
                                val line = lines[i].trim()
                                Log.d("BTDebug", "Line received: $line")
                                processSensorDataLine(line)
                            }
                            bluetoothReadBuffer.clear()
                            bluetoothReadBuffer.append(lines.last())
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Bluetooth read error — stream likely closed", e)
                handler.removeCallbacks(this)
                hasLoggedReadStart = false

                if (!isSimulating) {
                    isSimulating = true
                    handler.post(simulationRunnable)
                    handler.post { statusText.text = "Bluetooth disconnected, simulation ON" }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in readRunnable", e)
                handler.removeCallbacks(this)
            }

            handler.postDelayed(this, 5)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
        }

        footHeatMapView = findViewById(R.id.footHeatMapView)
        statusText = findViewById(R.id.statusText)
        toggleSimButton = findViewById(R.id.toggleSimButton)
        btnToggleView = findViewById(R.id.btnToggleView)
        btnExportCsv = findViewById(R.id.btnExportCsv)
        chart = findViewById(R.id.sensorChart)

        setupChart()

        btnToggleView.setOnClickListener {
            if (footHeatMapView.visibility == View.VISIBLE) {
                footHeatMapView.visibility = View.GONE
                chart.visibility = View.VISIBLE
                btnToggleView.text = "Switch to Foot Map"
            } else {
                chart.visibility = View.GONE
                footHeatMapView.visibility = View.VISIBLE
                btnToggleView.text = "Switch to Graph"
            }
        }

        btnExportCsv.setOnClickListener { exportCsv() }

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

        statusText.text = "Simulation mode ON"
        handler.post(simulationRunnable)
    }

    private val simulationRunnable = object : Runnable {
        override fun run() {
            val simulatedData = List(9) { (Math.random() * 1f).toFloat() }
            updateData(simulatedData)
            handler.postDelayed(this, 5)
        }
    }

    private fun updateData(pressures: List<Float>) {
        handler.post {
            footHeatMapView.updatePressures(pressures)
            appendDataToCsv(pressures)
            appendToChart(pressures[selectedSensorIndex])

            val gaitStatus =
                if (pressures.none { it > 0.85f || it < 0.1f }) "✅ Normal gait" else "⚠️ Abnormal pressure"
            statusText.text = "Streaming: ${pressures.joinToString(",")} | $gaitStatus"
        }
    }

    private fun appendDataToCsv(pressures: List<Float>) {
        val timestamp = System.currentTimeMillis()
        val line = "$timestamp,${pressures.joinToString(",")}\n"
        csvFile.appendText(line)
    }

    private fun setupChart() {
        dataSet = LineDataSet(mutableListOf(), "Sensor ${selectedSensorIndex + 1} Pressure")
        dataSet.color = Color.BLUE
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)

        lineData = LineData(dataSet)
        chart.data = lineData
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
    }

    private fun appendToChart(value: Float) {
        val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
        lineData.addEntry(Entry(elapsedSec, value), 0)
        lineData.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.setVisibleXRangeMaximum(5f)
        chart.moveViewToX(elapsedSec)
    }

    private fun exportCsv() {
        try {
            if (!csvFile.exists()) {
                Toast.makeText(this, "CSV file not found", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", csvFile)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share CSV"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to export CSV: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun checkPermissionsAndConnectBluetooth() {
        val missingPermissions = bluetoothPermissions.any {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions) requestBluetoothPermissionsLauncher.launch(bluetoothPermissions)
        else connectToRaspberryPi()
    }

    private val requestBluetoothPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) connectToRaspberryPi()
        else Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
    }

    private fun connectToRaspberryPi() {
        if (bluetoothSocket?.isConnected == true) {
            Log.w(TAG, "Already connected — skipping reconnection")
            return
        }

        val device = bluetoothAdapter?.getRemoteDevice(raspberryPiMac)
        if (device == null) {
            Toast.makeText(this, "Raspberry Pi device not found", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                Log.d(TAG, "Attempting Bluetooth connection...")

                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()

                Log.d(TAG, "Bluetooth socket connected")

                // Send start command to Pi
                bluetoothSocket?.outputStream?.write("start\n".toByteArray())
                Log.d(TAG, "Start command sent to Raspberry Pi")

                inputStream = bluetoothSocket?.inputStream
                reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

                handler.post {
                    statusText.text = "Connected to Raspberry Pi"
                    hasLoggedReadStart = false
                    handler.postDelayed(readRunnable, 200)
                }

            } catch (e: IOException) {
                Log.e(TAG, "Bluetooth connection failed", e)
                handler.post {
                    statusText.text = "Connection failed: ${e.message}"
                    isSimulating = true
                    handler.post(simulationRunnable)
                }
            }
        }.start()
    }

    private fun processSensorDataLine(line: String) {
        val parts = line.split(";")

        if (parts.size >= 9) {
            try {
                val pressures = parts.take(9).map { it.toFloatOrNull()?.coerceIn(0f, 1f) ?: 0f }
                Log.d(TAG, "Calling updateData with: ${pressures.joinToString(",")}")
                updateData(pressures)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse line: $line", e)
            }
        } else {
            Log.w(TAG, "Incomplete sensor data: $line")
        }

        Log.d(TAG, "Processing line: $line")
    }

    private val bluetoothConnectRunnable = Runnable { connectToRaspberryPi() }

    override fun onResume() {
        super.onResume()
        if (!isSimulating) {
            checkPermissionsAndConnectBluetooth()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "App is being destroyed — cleaning up")
        bluetoothSocket?.close()
        handler.removeCallbacks(readRunnable)
        handler.removeCallbacks(simulationRunnable)
        handler.removeCallbacks(bluetoothConnectRunnable)
    }
}