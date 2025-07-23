package com.example.myhack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myhack.uiApp.FootHeatMapView
import java.util.Random // <-- Use this for nextGaussian()
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var heatmapView: FootHeatMapView
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 second
    private val gaussianRandom = Random() // <-- Java Random for nextGaussian()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        heatmapView = findViewById(R.id.heatmapView)

        startPressureSimulation()
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
        val pressures = IntArray(18) { index ->
            val (mean, stdDev) = when (index) {
                in 0..2 -> 800.0 to 50.0   // Heel
                in 3..13 -> 400.0 to 100.0 // Mid-foot
                in 14..17 -> 600.0 to 70.0 // Forefoot/toes
                else -> 300.0 to 100.0
            }

            val value = mean + stdDev * gaussianRandom.nextGaussian()
            value.coerceIn(0.0, 1000.0).roundToInt()
        }

        heatmapView.updatePressures(pressures)

        val abnormalDetected = pressures.any { it < 100 }
        statusText.text = if (abnormalDetected) {
            "⚠️ Abnormal Pressure"
        } else {
            "✅ Status: Normal"
        }
    }
}
