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

    private val xOffset = 0.11f

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

        val bitmapRatio = footBitmap.width.toFloat() / footBitmap.height
        val viewRatio = viewWidth / viewHeight

        val drawWidth: Float
        val drawHeight: Float
        val left: Float
        val top: Float

        if (bitmapRatio > viewRatio) {
            drawWidth = viewWidth
            drawHeight = drawWidth / bitmapRatio
            left = 0f
            top = (viewHeight - drawHeight) / 2f
        } else {
            drawHeight = viewHeight
            drawWidth = drawHeight * bitmapRatio
            top = 0f
            left = (viewWidth - drawWidth) / 2f
        }

        val destRect = RectF(left, top, left + drawWidth, top + drawHeight)
        canvas.drawBitmap(footBitmap, null, destRect, null)

        // Dibujar sensores
        for ((i, pos) in sensorPositions.withIndex()) {
            val cx = left + pos.x * drawWidth
            val cy = top + pos.y * drawHeight

            val pressure = pressures.getOrElse(i) { 0f }
            paint.color = getPressureColor(pressure)
            val radius = 20f + pressure * 20f // círculo más grande si presión es alta
            canvas.drawCircle(cx, cy, radius, paint)
            canvas.drawText((i + 1).toString(), cx, cy + 10f, textPaint)
        }
    }

    private fun getPressureColor(value: Float): Int {
        val clamped = value.coerceIn(
            0f, 1f)
        return when {
            clamped > 0.85f -> Color.RED
            clamped > 0.6f -> Color.parseColor("#FF4500") // naranja fuerte
            clamped > 0.4f -> Color.YELLOW
            clamped > 0.2f -> Color.GREEN
            else -> Color.BLUE
        }
    }
}