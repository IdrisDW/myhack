package com.example.myhack // Change this if your app uses a different package

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var sensor1: View
    private lateinit var sensor2: View

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        sensor1 = findViewById(R.id.sensor1)
        sensor2 = findViewById(R.id.sensor2)

        startSimulation()
    }

    private fun startSimulation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                simulatePressure()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    private fun simulatePressure() {
        val pressure1 = Random.nextInt(0, 1000)
        val pressure2 = Random.nextInt(0, 1000)

        var abnormal = false

        fun updateSensor(view: View, pressure: Int) {
            when {
                pressure < 200 -> {
                    view.setBackgroundColor(0xFFFF0000.toInt()) // Red
                    abnormal = true
                }
                pressure < 600 -> {
                    view.setBackgroundColor(0xFFFFC107.toInt()) // Yellow
                }
                else -> {
                    view.setBackgroundColor(0xFF4CAF50.toInt()) // Green
                }
            }
        }

        updateSensor(sensor1, pressure1)
        updateSensor(sensor2, pressure2)

        statusText.text = if (abnormal) "⚠️ Abnormal Pressure" else "✅ Status: Normal"
    }
}
