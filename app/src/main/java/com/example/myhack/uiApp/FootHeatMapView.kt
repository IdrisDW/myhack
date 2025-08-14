package com.example.myhack.uiApp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.myhack.R

class FootHeatMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val xOffset = 0.11f // tweak this number from 0 to ~0.15

    private val sensorPositions = listOf(
        PointF(0.25f + xOffset, 0.90f),
        PointF(0.40f + xOffset, 0.90f),
        PointF(0.22f + xOffset, 0.75f),
        PointF(0.45f + xOffset, 0.75f),
        PointF(0.18f + xOffset, 0.55f),
        PointF(0.33f + xOffset, 0.50f),
        PointF(0.48f + xOffset, 0.48f),
        PointF(0.63f + xOffset, 0.47f),
        PointF(0.70f + xOffset, 0.40f)
    )


    private var pressures: List<Float> = List(sensorPositions.size) { 0f }

    private val footBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.foot_outline)

    fun updatePressures(newPressures: List<Float>) {
        if (newPressures.size == sensorPositions.size) {
            pressures = newPressures
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // Calculate scale to fit bitmap inside view bounds (contain)
        val bitmapRatio = footBitmap.width.toFloat() / footBitmap.height
        val viewRatio = viewWidth / viewHeight

        val drawWidth: Float
        val drawHeight: Float
        val left: Float
        val top: Float

        if (bitmapRatio > viewRatio) {
            // Bitmap is wider relative to view - fit width
            drawWidth = viewWidth
            drawHeight = drawWidth / bitmapRatio
            left = 0f
            top = (viewHeight - drawHeight) / 2f
        } else {
            // Bitmap is taller relative to view - fit height
            drawHeight = viewHeight
            drawWidth = drawHeight * bitmapRatio
            top = 0f
            left = (viewWidth - drawWidth) / 2f
        }

        // Destination rectangle where bitmap is drawn
        val destRect = RectF(left, top, left + drawWidth, top + drawHeight)
        canvas.drawBitmap(footBitmap, null, destRect, null)

        // Draw pressure dots inside the bitmap rect
        for ((i, pos) in sensorPositions.withIndex()) {
            val cx = left + pos.x * drawWidth
            val cy = top + pos.y * drawHeight

            val pressure = pressures.getOrElse(i) { 0f }
            paint.color = getPressureColor(pressure)

            canvas.drawCircle(cx, cy, 25f, paint)
            canvas.drawText((i + 1).toString(), cx, cy + 10f, textPaint)
        }
    }

    private fun getPressureColor(value: Float): Int {
        val clamped = value.coerceIn(0f, 1f)
        val red = (clamped * 255).toInt()
        val green = ((1 - clamped) * 255).toInt()
        return Color.rgb(red, green, 0)
    }
}
