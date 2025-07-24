package com.example.myhack

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

fun Context.appendDataToCsv(dataLine: String, fileName: String = "sensor_data.csv") {
    try {
        val file = File(this.filesDir, fileName)
        val isNewFile = !file.exists()

        FileOutputStream(file, true).use { fos ->
            OutputStreamWriter(fos).use { writer ->
                if (isNewFile) {
                    writer.append("timestamp," + (1..18).joinToString(",") { "sensor$it" } + ",gait_type")
                    writer.append("\n")
                }
                writer.append(dataLine)
                writer.append("\n")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
