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
    private var sensorValues = List(18) { 0 }
    private var footBitmap: Bitmap? = null

    // Map of 18 sensor positions (x, y in normalized 0..1)
    private val sensorPositions = listOf(
        // === 7 Paper-Based Functional Sensors ===
        0.50f to 0.85f, // S1: Heel Center (Large) [Paper: Heel]
        0.35f to 0.60f, // S2: 1st Metatarsal (Medium) [Paper]
        0.65f to 0.60f, // S3: 5th Metatarsal (Medium) [Paper]
        0.40f to 0.25f, // S4: Hallux / Big Toe (Medium) [Paper]
        0.50f to 0.50f, // S5: 3rd Metatarsal (Small) [Paper]
        0.45f to 0.65f, // S6: Medial Arch (Small) [Paper]
        0.55f to 0.65f, // S7: Lateral Arch (Small) [Paper]

        // === 11 Supplemental Sensors ===
        0.30f to 0.85f, // S8: Heel Left (Large)
        0.50f to 0.70f, // S9: Midfoot Center (Medium)
        0.40f to 0.70f, // S10: Midfoot Left (Small)
        0.60f to 0.70f, // S11: Midfoot Right (Small)
        0.35f to 0.20f, // S12: Toe 2 (Small)
        0.45f to 0.20f, // S13: Toe 3 (Small)
        0.55f to 0.20f, // S14: Toe 4 (Small)
        0.60f to 0.55f, // S15: Lateral Forefoot (Medium)
        0.40f to 0.55f, // S16: Medial Forefoot (Medium)
        0.42f to 0.60f, // S17: Arch Edge Left (Medium)
        0.58f to 0.60f  // S18: Arch Edge Right (Medium)
    )

    init {
        footBitmap = BitmapFactory.decodeResource(resources, R.drawable.foot)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw foot image centered
        footBitmap?.let { bmp ->
            val left = (width - bmp.width) / 2f
            val top = (height - bmp.height) / 2f
            canvas.drawBitmap(bmp, left, top, null)
        }

        val cx = width.toFloat()
        val cy = height.toFloat()

        for (i in sensorValues.indices) {
            val (nx, ny) = sensorPositions[i]
            val x = nx * cx
            val y = ny * cy

            paint.color = getColorForValue(sensorValues[i])
            canvas.drawCircle(x, y, 18f, paint)
        }
    }

    private fun getColorForValue(value: Int): Int {
        val clamped = value.coerceIn(0, 1023)
        val ratio = clamped / 1023f
        return Color.rgb((255 * ratio).toInt(), (255 * (1 - ratio)).toInt(), 0)
    }

    fun updateSensorValues(newValues: List<Int>) {
        if (newValues.size == 18) {
            sensorValues = newValues
            invalidate()
        }
    }
}
