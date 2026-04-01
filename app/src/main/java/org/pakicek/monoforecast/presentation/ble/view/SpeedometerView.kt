package org.pakicek.monoforecast.presentation.ble.view

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import org.pakicek.monoforecast.R
import kotlin.math.min

class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var maxSpeed = 60f
    private var currentSpeed = 0f

    private val arcColor = ContextCompat.getColor(context, R.color.purple_200)
    private val bgColor = "#404040".toColorInt()

    private val textColor: Int by lazy {
        val typedValue = TypedValue()
        val theme = context.theme
        if (theme.resolveAttribute(android.R.attr.textColor, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                ContextCompat.getColor(context, typedValue.resourceId)
            } else {
                typedValue.data
            }
        } else {
            Color.WHITE
        }
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    private val textPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = textColor
            textAlign = Paint.Align.CENTER
        }
    }

    private val rect = RectF()

    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0f, maxSpeed)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(width, height)

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val size = min(w, h)
        val strokeWidth = size * 0.08f
        val padding = strokeWidth / 2 + 10f

        rect.set(padding, padding, w - padding, h - padding)

        paint.color = bgColor
        paint.strokeWidth = strokeWidth
        canvas.drawArc(rect, 150f, 240f, false, paint)

        paint.color = arcColor
        val sweepAngle = (currentSpeed / maxSpeed) * 240f
        canvas.drawArc(rect, 150f, sweepAngle, false, paint)

        textPaint.textSize = size * 0.25f
        textPaint.isFakeBoldText = true
        canvas.drawText(
            currentSpeed.toInt().toString(),
            w / 2,
            h / 2 + (textPaint.textSize / 3),
            textPaint
        )

        textPaint.textSize = size * 0.08f
        textPaint.isFakeBoldText = false
        canvas.drawText("km/h", w / 2, h / 2 + (size * 0.2f), textPaint)
    }
}