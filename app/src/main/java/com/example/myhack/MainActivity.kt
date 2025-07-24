package com.example.myhack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myhack.uiApp.FootHeatMapView
import java.util.Random
import kotlin.math.roundToInt

enum class GaitType {
    NORMAL,
    NEUROPATHY,
    DROP_FOOT
}

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var heatmapView: FootHeatMapView
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L
    private val random = Random()

    private var currentGait = GaitType.NORMAL
    private var counter = 0

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
                val pressures = when (currentGait) {
                    GaitType.NORMAL -> simulateNormalGaitData()
                    GaitType.NEUROPATHY -> simulateNeuropathyData()
                    GaitType.DROP_FOOT -> simulateDropFootData()
                }

                heatmapView.updatePressures(pressures)

                val abnormalDetected = pressures.any { it < 100 || it > 900 }
                statusText.text = when (currentGait) {
                    GaitType.NORMAL -> if (abnormalDetected) "⚠️ Abnormal Pressure Detected" else "✅ Normal Gait"
                    GaitType.NEUROPATHY -> "🧠 Neuropathy Simulation"
                    GaitType.DROP_FOOT -> "🦶 Drop Foot Simulation"
                }

                val timestamp = System.currentTimeMillis()
                val dataLine = "$timestamp,${pressures.joinToString(",")},${currentGait.name}"
                appendDataToCsv(dataLine)

                Log.d("CSV", "Saved: $dataLine")

                toggleSimulation()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    private fun toggleSimulation() {
        counter++
        currentGait = when ((counter / 10) % 3) {
            0 -> GaitType.NORMAL
            1 -> GaitType.NEUROPATHY
            else -> GaitType.DROP_FOOT
        }
    }

    private fun simulateNormalGaitData(): IntArray {
        return IntArray(18) { index ->
            val (mean, stdDev) = when (index) {
                in 0..2 -> 800.0 to 50.0
                in 3..13 -> 400.0 to 100.0
                in 14..17 -> 600.0 to 70.0
                else -> 300.0 to 100.0
            }
            val value = mean + stdDev * random.nextGaussian()
            value.coerceIn(0.0, 1000.0).roundToInt()
        }
    }

    private fun simulateNeuropathyData(): IntArray {
        return IntArray(18) { index ->
            val baseMean = when (index) {
                in 0..2 -> 850.0
                in 3..13 -> 300.0
                in 14..17 -> 650.0
                else -> 300.0
            }
            val baseStdDev = 150.0

            var value = baseMean + baseStdDev * random.nextGaussian()
            val spikeChance = random.nextDouble()
            if (spikeChance < 0.15) {
                value += if (random.nextBoolean()) 300 else -300
            }
            value.coerceIn(0.0, 1000.0).roundToInt()
        }
    }

    private fun simulateDropFootData(): IntArray {
        return IntArray(18) { index ->
            val (mean, stdDev) = when (index) {
                in 0..2 -> 850.0 to 70.0      // Heel strong impact
                in 3..13 -> 350.0 to 120.0    // Midfoot irregular
                in 14..17 -> 150.0 to 80.0    // Toes underused
                else -> 300.0 to 100.0
            }
            val value = mean + stdDev * random.nextGaussian()
            value.coerceIn(0.0, 1000.0).roundToInt()
        }
    }
}
