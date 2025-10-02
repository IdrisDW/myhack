package com.example.myhack.uiApp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ScaleBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val labels = listOf("0.0", "0.2", "0.4", "0.6", "0.8", "1.0")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barHeight = height * 0.3f
        val barTop = height * 0.2f
        val barBottom = barTop + barHeight
        val barLeft = paddingLeft.toFloat()
        val barRight = width - paddingRight.toFloat()

        // Create gradient from blue to red
        val gradient = LinearGradient(
            barLeft, barTop, barRight, barTop,
            intArrayOf(
                Color.BLUE,
                Color.CYAN,
                Color.GREEN,
                Color.YELLOW,
                Color.parseColor("#FFA500"), // orange
                Color.RED
            ),
            null,
            Shader.TileMode.CLAMP
        )
        gradientPaint.shader = gradient

        // Draw gradient bar
        canvas.drawRect(barLeft, barTop, barRight, barBottom, gradientPaint)

        // Draw labels
        val labelY = barBottom + 40f
        val segmentWidth = (barRight - barLeft) / (labels.size - 1)
        for ((i, label) in labels.withIndex()) {
            val x = barLeft + i * segmentWidth
            canvas.drawText(label, x, labelY, labelPaint)
        }
    }
}
