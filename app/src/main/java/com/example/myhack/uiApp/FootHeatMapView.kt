package com.example.myhack.uiApp

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.myhack.R
import kotlin.math.roundToInt
import android.graphics.Bitmap


class FootHeatMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pressures: List<Int> = List(18) { 0 }
    private val maxPressure = 1023f
    private val paint = Paint()
    private val footBitmap = BitmapFactory.decodeResource(resources, R.drawable.foot_outline)

    fun updatePressures(newPressures: List<Int>) {
        pressures = newPressures
        invalidate()
    }

    private val sensorPositions = listOf(
        Pair(0.22f, 0.85f), // H1 - Medial Heel
        Pair(0.78f, 0.85f), // H2 - Lateral Heel
        Pair(0.30f, 0.65f), // M1 - 1st Metatarsal
        Pair(0.50f, 0.63f), // M3 - 3rd Metatarsal
        Pair(0.68f, 0.65f), // M5 - 5th Metatarsal
        Pair(0.35f, 0.30f), // T1 - Big Toe
        Pair(0.65f, 0.32f), // T5 - 5th Toe

        // Extras
        Pair(0.50f, 0.80f), // EXTRA1
        Pair(0.40f, 0.72f), // EXTRA2
        Pair(0.60f, 0.72f), // EXTRA3
        Pair(0.25f, 0.55f), // EXTRA4
        Pair(0.75f, 0.55f), // EXTRA5
        Pair(0.20f, 0.40f), // EXTRA6
        Pair(0.80f, 0.40f), // EXTRA7
        Pair(0.40f, 0.20f), // EXTRA8
        Pair(0.60f, 0.20f), // EXTRA9
        Pair(0.50f, 0.10f), // EXTRA10
        Pair(0.50f, 0.35f)  // EXTRA11
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val imageRatio = footBitmap.width.toFloat() / footBitmap.height
        val displayWidth = width.toFloat()
        val displayHeight = displayWidth / imageRatio
        val top = (height - displayHeight) / 2

        canvas.drawBitmap(
            Bitmap.createScaledBitmap(footBitmap, displayWidth.toInt(), displayHeight.toInt(), true),
            0f,
            top,
            null
        )

        pressures.forEachIndexed { index, pressure ->
            val (xFactor, yFactor) = sensorPositions.getOrNull(index) ?: return@forEachIndexed
            val x = xFactor * displayWidth
            val y = top + yFactor * displayHeight

            val intensity = (pressure / maxPressure).coerceIn(0f, 1f)
            paint.color = Color.argb(
                (100 + intensity * 155).roundToInt(),
                (255 * (1 - intensity)).roundToInt(),
                0,
                (255 * intensity).roundToInt()
            )

            canvas.drawCircle(x, y, 25f, paint)
        }
    }
}
