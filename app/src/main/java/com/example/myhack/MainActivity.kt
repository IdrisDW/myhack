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

class MainActivity : AppCompatActivity() {

    private lateinit var footHeatMapView: FootHeatMapView
    private lateinit var statusText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val updateTask = object : Runnable {
        override fun run() {
            val simulatedData = generateSimulatedPressureData()
            footHeatMapView.updatePressures(simulatedData)
            statusText.text = if (isPressureNormal(simulatedData)) "✅ Normal gait" else "⚠️ Abnormal pressure"
            handler.postDelayed(this, 1000) // update every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        footHeatMapView = findViewById(R.id.heatmapView)
        statusText = findViewById(R.id.statusText)

        handler.post(updateTask)
    }

    private fun generateSimulatedPressureData(): List<Float> {
        val random = Random()
        return List(18) {
            (random.nextFloat() * 100).roundToInt() / 100f
        }
    }

    private fun isPressureNormal(data: List<Float>): Boolean {
        return data.none { it > 0.85f || it < 0.1f }
    }
}
