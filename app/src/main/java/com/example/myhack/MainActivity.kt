package com.example.myhack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myhack.uiApp.FootHeatMapView
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var footHeatMapView: FootHeatMapView
    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1000 // milliseconds
    private val sensorCount = 18

    private val csvFile: File by lazy {
        val fileName = "sensor_data.csv"
        val file = File(filesDir, fileName)
        if (!file.exists()) file.writeText("timestamp," + (0 until sensorCount).joinToString(",") { "Sensor${it + 1}" } + "\n")
        file
    }

    private val updateTask = object : Runnable {
        override fun run() {
            val pressures = generateFakePressures()
            footHeatMapView.updatePressures(pressures)
            appendDataToCsv(pressures)
            handler.postDelayed(this, interval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        footHeatMapView = findViewById(R.id.footHeatMap)
        handler.post(updateTask)
    }

    private fun generateFakePressures(): List<Int> {
        return List(sensorCount) { Random.nextInt(100, 1024) }
    }

    private fun appendDataToCsv(data: List<Int>) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val line = "$timestamp," + data.joinToString(",") + "\n"
        FileWriter(csvFile, true).use { it.write(line) }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTask)
    }
}
