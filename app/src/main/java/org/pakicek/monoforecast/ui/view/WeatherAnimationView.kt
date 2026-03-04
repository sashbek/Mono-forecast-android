package org.pakicek.monoforecast.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.pakicek.monoforecast.domain.model.dto.enums.WeatherCondition
import kotlin.random.Random
import androidx.core.graphics.toColorInt

class WeatherAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var condition: WeatherCondition = WeatherCondition.CLEAR
    private val particles = mutableListOf<Particle>()
    private val rainColor = "#40C4FF".toColorInt()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var speed: Float,
        var size: Float,
        var alpha: Int
    )

    fun setWeatherCondition(newCondition: WeatherCondition) {
        if (condition == newCondition) return
        condition = newCondition
        particles.clear()

        val count = when (condition) {
            WeatherCondition.RAIN -> 150
            WeatherCondition.SNOW -> 50
            else -> 0
        }

        repeat(count) {
            particles.add(createRandomParticle(true))
        }
        invalidate()
    }

    private fun createRandomParticle(randomY: Boolean): Particle {
        val w = width.toFloat().takeIf { it > 0 } ?: 1080f
        val h = height.toFloat().takeIf { it > 0 } ?: 1920f

        val isRain = condition == WeatherCondition.RAIN

        return Particle(
            x = Random.nextFloat() * w,
            y = if (randomY) Random.nextFloat() * h else -50f,
            speed = if (isRain) Random.nextFloat() * 20f + 25f else Random.nextFloat() * 2f + 2f,
            size = if (isRain) Random.nextFloat() * 2f + 3f else Random.nextFloat() * 5f + 5f,
            alpha = if (isRain) Random.nextInt(180, 255) else Random.nextInt(100, 200)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (condition == WeatherCondition.CLEAR || condition == WeatherCondition.CLOUDY) return
        val h = height

        if (condition == WeatherCondition.RAIN) {
            paint.color = rainColor
            paint.strokeWidth = 4f
        } else {
            paint.color = Color.WHITE
            paint.strokeWidth = 0f
        }

        particles.forEach { p ->
            paint.alpha = p.alpha
            if (condition == WeatherCondition.RAIN) {
                canvas.drawLine(p.x, p.y, p.x, p.y + p.size * 8, paint)
            } else {
                canvas.drawCircle(p.x, p.y, p.size, paint)
            }

            p.y += p.speed
            if (p.y > h) {
                val newP = createRandomParticle(false)
                p.y = newP.y
                p.x = newP.x
                p.speed = newP.speed
            }
        }

        postInvalidateOnAnimation()
    }
}