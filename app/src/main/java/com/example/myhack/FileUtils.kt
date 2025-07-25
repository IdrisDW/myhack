package com.example.myhack

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun appendSensorData(context: Context, pressures: List<Int>) {
        val fileName = "sensor_data.csv"
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            val header = "timestamp," + pressures.indices.joinToString(",") { "Sensor${it + 1}" } + "\n"
            file.writeText(header)
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val line = "$timestamp," + pressures.joinToString(",") + "\n"
        FileWriter(file, true).use {
            it.write(line)
        }
    }
}
