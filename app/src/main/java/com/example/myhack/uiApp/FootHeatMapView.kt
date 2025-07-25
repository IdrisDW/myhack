package com.example.myhack.uiApp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.myhack.R

class FootHeatMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint()
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private var pressures: List<Float> = List(18) { 0f }
    private var footBitmap: Bitmap? = null

    // Adjusted and labeled sensor positions (0-1 normalized)
    private val sensorPositions = listOf(
        PointF(0.48f, 0.13f), // 1 - Hallux
        PointF(0.62f, 0.20f), // 2 - Toes 2–5 [Adjusted]
        PointF(0.28f, 0.33f), // 3 - 1st Metatarsal
        PointF(0.48f, 0.38f), // 4 - 3rd Metatarsal
        PointF(0.68f, 0.33f), // 5 - 5th Metatarsal
        PointF(0.44f, 0.68f), // 6 - Midfoot
        PointF(0.50f, 0.89f), // 7 - Heel center

        // Extra sensors
        PointF(0.22f, 0.20f), // 8 - Extra lateral toe
        PointF(0.72f, 0.22f), // 9 - Extra medial toe [Adjusted]
        PointF(0.2f, 0.38f),  // 10 - Extra mid lateral met
        PointF(0.8f, 0.38f),  // 11 - Extra mid medial met
        PointF(0.36f, 0.53f), // 12 - Extra arch left
        PointF(0.64f, 0.53f), // 13 - Extra arch right
        PointF(0.3f, 0.76f),  // 14 - Extra heel left
        PointF(0.7f, 0.76f),  // 15 - Extra heel right
        PointF(0.30f, 0.63f), // 16 - Extra outer foot [Adjusted]
        PointF(0.75f, 0.60f), // 17 - Extra inner foot
        PointF(0.50f, 0.58f)  // 18 - Extra center arch
    )

    init {
        footBitmap = BitmapFactory.decodeResource(resources, R.drawable.foot_outline)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // Draw foot outline bitmap centered
        footBitmap?.let { bitmap ->
            val imageRatio = bitmap.width.toFloat() / bitmap.height
            val viewRatio = viewWidth / viewHeight
            val drawWidth: Float
            val drawHeight: Float
            val left: Float
            val top: Float

            if (imageRatio > viewRatio) {
                drawWidth = viewWidth
                drawHeight = drawWidth / imageRatio
                left = 0f
                top = (viewHeight - drawHeight) / 2f
            } else {
                drawHeight = viewHeight
                drawWidth = drawHeight * imageRatio
                left = (viewWidth - drawWidth) / 2f
                top = 0f
            }

            val destRect = RectF(left, top, left + drawWidth, top + drawHeight)
            canvas.drawBitmap(bitmap, null, destRect, null)

            // Draw sensors
            for (i in sensorPositions.indices) {
                val pos = sensorPositions[i]
                val x = left + pos.x * drawWidth
                val y = top + pos.y * drawHeight
                val pressure = pressures.getOrElse(i) { 0f }

                paint.color = getColorForPressure(pressure)
                canvas.drawCircle(x, y, 20f, paint)

                // Optional: draw sensor number for debugging
                canvas.drawText((i + 1).toString(), x, y - 25f, textPaint)
            }
        }
    }

    private fun getColorForPressure(pressure: Float): Int {
        val clamped = pressure.coerceIn(0f, 1f)
        val red = (clamped * 255).toInt()
        val green = ((1 - clamped) * 255).toInt()
        return Color.rgb(red, green, 0)
    }

    fun updatePressures(newPressures: List<Float>) {
        pressures = newPressures
        invalidate()
    }
}
