package com.example.myhack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var sensorGrid: GridLayout
    private val sensorViews = mutableListOf<View>()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        sensorGrid = findViewById(R.id.sensorGrid)

        // Dynamically create 18 sensor views and add them to the GridLayout
        for (i in 1..18) {
            val sensorView = View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 100  // px, adjust to your needs or convert dp to px
                    height = 100
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundResource(R.drawable.circle_green) // default green color
                id = View.generateViewId() // unique id just in case
            }
            sensorGrid.addView(sensorView)
            sensorViews.add(sensorView)
        }

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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)  // Clean up handler callbacks
    }
}
