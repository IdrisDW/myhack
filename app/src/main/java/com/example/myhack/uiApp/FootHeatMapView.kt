package com.example.myhack.uiApp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class FootHeatMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val sensorData = IntArray(18) { 0 }

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    private val sensorPositions = listOf(
        // Approximate positions on the foot (x%, y%)
        Pair(0.2f, 0.1f), Pair(0.5f, 0.1f), Pair(0.8f, 0.1f), // Toes
        Pair(0.3f, 0.25f), Pair(0.5f, 0.25f), Pair(0.7f, 0.25f),
        Pair(0.25f, 0.4f), Pair(0.5f, 0.4f), Pair(0.75f, 0.4f),
        Pair(0.3f, 0.55f), Pair(0.5f, 0.55f), Pair(0.7f, 0.55f),
        Pair(0.3f, 0.7f), Pair(0.5f, 0.7f), Pair(0.7f, 0.7f),
        Pair(0.3f, 0.85f), Pair(0.5f, 0.85f), Pair(0.7f, 0.85f)
    )

    fun updateSensorData(newData: IntArray) {
        if (newData.size == 18) {
            for (i in newData.indices) {
                sensorData[i] = newData[i]
            }
            invalidate()
        }
    }

    private fun getColorForPressure(value: Int): Int {
        return when {
            value < 100 -> Color.RED
            value < 400 -> Color.YELLOW
            else -> Color.GREEN
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = min(width, height) / 20f

        for (i in sensorPositions.indices) {
            val (xPercent, yPercent) = sensorPositions[i]
            val x = width * xPercent
            val y = height * yPercent

            paint.color = getColorForPressure(sensorData[i])
            canvas.drawCircle(x, y, radius, paint)
        }
    }
}
