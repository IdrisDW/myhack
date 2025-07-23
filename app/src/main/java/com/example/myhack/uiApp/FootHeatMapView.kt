package com.example.myhack.uiApp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myhack.R

class FootHeatMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val footBitmap = BitmapFactory.decodeResource(resources, R.drawable.foot)
    private var pressureValues = IntArray(18) { 0 }

    // Example sensor positions in percentage of the view size (x,y from 0 to 1)
    private val sensorPositions = arrayOf(
        PointF(0.3f, 0.9f), PointF(0.7f, 0.9f),  // Heel
        PointF(0.25f, 0.75f), PointF(0.5f, 0.75f), PointF(0.75f, 0.75f), // Midfoot
        PointF(0.2f, 0.6f), PointF(0.4f, 0.6f), PointF(0.6f, 0.6f), PointF(0.8f, 0.6f), // Forefoot
        PointF(0.15f, 0.45f), PointF(0.3f, 0.45f), PointF(0.45f, 0.45f), PointF(0.6f, 0.45f), PointF(0.75f, 0.45f), // Toes
        PointF(0.25f, 0.3f), PointF(0.5f, 0.3f), PointF(0.75f, 0.3f), PointF(0.5f, 0.2f) // Extra precision
    )

    fun updatePressures(pressures: IntArray) {
        if (pressures.size == 18) {
            pressureValues = pressures
            invalidate() // Redraw view
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw foot outline scaled to view size
        val footRect = Rect(0, 0, width, height)
        canvas.drawBitmap(footBitmap, null, footRect, paint)

        // Draw heat spots for each sensor
        for (i in pressureValues.indices) {
            val pressure = pressureValues[i]
            val pos = sensorPositions[i]

            val x = pos.x * width
            val y = pos.y * height

            val color = pressureToColor(pressure)
            paint.color = color
            paint.style = Paint.Style.FILL

            // Radius scaled by pressure (pressure 0-1000 mapped to 10-50 pixels radius)
            val radius = 10 + (pressure / 1000f) * 40f

            // Draw blurred circle for heat spot
            paint.maskFilter = BlurMaskFilter(radius / 2, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(x, y, radius, paint)

            // Draw solid circle center for clarity
            paint.maskFilter = null
            paint.alpha = 200
            canvas.drawCircle(x, y, radius / 2, paint)
            paint.alpha = 255
        }
    }

    // Map pressure (0-1000) to a color gradient (red->yellow->green)
    private fun pressureToColor(pressure: Int): Int {
        return when {
            pressure < 100 -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
            pressure < 400 -> ContextCompat.getColor(context, android.R.color.holo_orange_light)
            else -> ContextCompat.getColor(context, android.R.color.holo_green_light)
        }
    }
}
