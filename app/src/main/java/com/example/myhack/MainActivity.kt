package com.example.myhack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private val sensorViews = mutableListOf<View>()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 second

    // Add a sessionId for this data session
    private val currentSessionId = System.currentTimeMillis().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)

        for (i in 1..18) {
            val viewId = resources.getIdentifier("sensor$i", "id", packageName)
            val sensorView = findViewById<View>(viewId)
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

        for ((index, view) in sensorViews.withIndex()) {
            val pressure = Random.nextInt(0, 1000)

            // Create SensorReading with sessionId included
            val sensorReading = com.example.myhack.data.SensorReading(
                sensorId = index + 1,
                pressure = pressure,
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId
            )

            // TODO: Insert sensorReading into database when ready

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
}
