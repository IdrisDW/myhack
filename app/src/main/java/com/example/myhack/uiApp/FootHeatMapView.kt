package com.example.myhack.uiApp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.myhack.R
import kotlin.math.min

class FootHeatMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val sensorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
    }

    private val footOutlineBitmap = BitmapFactory.decodeResource(resources, R.drawable.foot_outline)
    private val footRect = Rect()

    // Initialize with 18 dummy values
    private var pressures: List<Float> = List(18) { 0.0f }

    // Sensor positions: 7 paper-based + 11 extra
    private val sensorPositions = listOf(
        // --- Paper-based sensors (positions from research article) ---
        PointF(0.5f, 0.15f), // 1 - Hallux (big toe)
        PointF(0.7f, 0.2f),  // 2 - Toes 2-5
        PointF(0.2f, 0.35f), // 3 - 1st metatarsal
        PointF(0.5f, 0.4f),  // 4 - 3rd metatarsal
        PointF(0.8f, 0.35f), // 5 - 5th metatarsal
        PointF(0.4f, 0.7f),  // 6 - Midfoot
        PointF(0.5f, 0.9f),  // 7 - Heel center

        // --- Extra sensors for full-foot coverage ---
        PointF(0.1f, 0.2f),  // 8 - Extra lateral toe
        PointF(0.9f, 0.2f),  // 9 - Extra medial toe
        PointF(0.1f, 0.4f),  // 10 - Extra mid lateral met
        PointF(0.9f, 0.4f),  // 11 - Extra mid medial met
        PointF(0.3f, 0.55f), // 12 - Extra arch left
        PointF(0.7f, 0.55f), // 13 - Extra arch right
        PointF(0.2f, 0.8f),  // 14 - Extra heel left
        PointF(0.8f, 0.8f),  // 15 - Extra heel right
        PointF(0.05f, 0.6f), // 16 - Extra outer foot
        PointF(0.95f, 0.6f), // 17 - Extra inner foot
        PointF(0.5f, 0.6f)   // 18 - Extra center arch
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the foot image, scaled to fit
        val scale = min(width / footOutlineBitmap.width.toFloat(), height / footOutlineBitmap.height.toFloat())
        val bitmapWidth = (footOutlineBitmap.width * scale).toInt()
        val bitmapHeight = (footOutlineBitmap.height * scale).toInt()
        val left = (width - bitmapWidth) / 2
        val top = (height - bitmapHeight) / 2
        footRect.set(left, top, left + bitmapWidth, top + bitmapHeight)
        canvas.drawBitmap(footOutlineBitmap, null, footRect, null)

        // Draw each sensor
        for ((index, point) in sensorPositions.withIndex()) {
            val cx = footRect.left + point.x * footRect.width()
            val cy = footRect.top + point.y * footRect.height()
            val pressure = pressures.getOrNull(index) ?: 0f

            // Set sensor color based on pressure value
            sensorPaint.color = getPressureColor(pressure)

            canvas.drawCircle(cx, cy, 18f, sensorPaint)

            // Optional: Label each sensor for debugging
            canvas.drawText("${index + 1}", cx + 15f, cy + 5f, textPaint)
        }
    }

    fun updatePressures(newPressures: List<Float>) {
        pressures = newPressures
        invalidate()
    }

    private fun getPressureColor(value: Float): Int {
        // Normalize value between 0 and 1
        val v = value.coerceIn(0f, 1f)

        val red = (255 * v).toInt()
        val green = (255 * (1 - v)).toInt()
        return Color.rgb(red, green, 0)
    }
}
