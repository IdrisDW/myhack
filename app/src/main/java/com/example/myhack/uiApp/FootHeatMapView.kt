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

    // Based on normalized x,y (0-1) relative to your foot_outline image (292x730)
    private val sensorPositions = listOf(
        PointF(0.25f, 0.90f),  // 1. Heel lateral
        PointF(0.40f, 0.90f),  // 2. Heel medial
        PointF(0.22f, 0.75f),  // 3. Midfoot lateral
        PointF(0.45f, 0.75f),  // 4. Midfoot medial
        PointF(0.18f, 0.55f),  // 5. 1st metatarsal head
        PointF(0.33f, 0.50f),  // 6. 2nd metatarsal head
        PointF(0.48f, 0.48f),  // 7. 3rd metatarsal head
        PointF(0.63f, 0.47f),  // 9. 4th metatarsal head
        PointF(0.70f, 0.40f)   // 13. Lateral forefoot
    )

    init {
        footBitmap = BitmapFactory.decodeResource(resources, R.drawable.foot_outline)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

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

            val horizontalOffset = 70f  // Adjust this to shift points horizontally (in pixels)

            for (i in sensorPositions.indices) {
                val pos = sensorPositions[i]
                val x = left + pos.x * drawWidth + horizontalOffset
                val y = top + pos.y * drawHeight
                val pressure = pressures.getOrElse(i) { 0f }

                paint.color = getColorForPressure(pressure)
                canvas.drawCircle(x, y, 20f, paint)
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
