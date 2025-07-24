package com.example.myhack.uiApp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myhack.R
import kotlin.math.min

class FootHeatMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private var pressures: IntArray = IntArray(18) { 0 }

    private var footBitmap: Bitmap? = null

    private val sensorPositions = listOf(
        0.5f to 0.10f,
        0.4f to 0.12f, 0.6f to 0.12f,
        0.35f to 0.28f, 0.5f to 0.28f, 0.65f to 0.28f,
        0.3f to 0.42f, 0.4f to 0.42f, 0.5f to 0.42f,
        0.6f to 0.42f, 0.7f to 0.42f,
        0.25f to 0.58f, 0.35f to 0.58f, 0.45f to 0.58f,
        0.55f to 0.58f, 0.65f to 0.58f, 0.75f to 0.58f,
        0.5f to 0.75f
    )

    init {
        // Load foot image from drawable
        footBitmap = BitmapFactory.decodeResource(resources, R.drawable.foot)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Draw the foot background image, scaled to fit
        footBitmap?.let { bitmap ->
            val dstRect = RectF(0f, 0f, width, height)
            canvas.drawBitmap(bitmap, null, dstRect, null)
        }

        // Radius of pressure circles
        val radius = min(width, height) * 0.04f

        // Draw pressure points
        for (i in pressures.indices) {
            if (i >= sensorPositions.size) continue

            val (xFactor, yFactor) = sensorPositions[i]
            val x = xFactor * width
            val y = yFactor * height
            val color = getColorForPressure(pressures[i])

            paint.color = color
            canvas.drawCircle(x, y, radius, paint)
        }
    }

    fun updatePressures(newPressures: IntArray) {
        pressures = newPressures.copyOf()
        invalidate()
    }

    private fun getColorForPressure(value: Int): Int {
        val clamped = value.coerceIn(0, 1000)
        val ratio = clamped / 1000f
        val red = (255 * ratio).toInt()
        val green = (255 * (1 - ratio)).toInt()
        val blue = (255 * (0.5f - kotlin.math.abs(ratio - 0.5f)) * 2).toInt()
        return Color.rgb(red, green, blue)
    }
}
