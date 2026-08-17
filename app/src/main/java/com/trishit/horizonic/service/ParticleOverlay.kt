package com.trishit.horizonic.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.trishit.horizonic.MotionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin
import kotlin.math.sqrt

class ParticleOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val random = Random()
    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var gyroX = 0f
    private var gyroY = 0f
    private var gyroZ = 0f

    private var targetGyroX = 0f
    private var targetGyroY = 0f
    private var targetGyroZ = 0f

    private var cachedTheme = ""
    private var cachedBaseCount = 4
    private var cachedSensitivity = 2.5f
    private var cachedParticleSize = 6f
    private var isOverlayActive = true

    private var activeColors = intArrayOf(0xFF00ACC1.toInt(), 0xFF00897B.toInt(), 0xFF4DB6AC.toInt(), 0xFF80CBC4.toInt())

    private var configJob: Job? = null
    private var frameTime = 0f

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!isAttachedToWindow) return
            if (isOverlayActive) {
                updatePhysics()
                invalidate()
            }
            postOnAnimation(this)
        }
    }

    enum class ParticleState {
        ACTIVE, FADING_OUT, FADING_IN
    }

    class Particle(
        val homeX: Float,
        val homeY: Float,
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        val color: Int,
        var alpha: Float, // 0.0 to 1.0
        var alphaOffset: Float,
        var isExtra: Boolean = false,
        var state: ParticleState = ParticleState.ACTIVE
    )

    init {
        updateThemeColors(MotionState.particleColorTheme.value)
        cachedBaseCount = MotionState.baseParticlesPerSide.value
        cachedSensitivity = MotionState.gyroSensitivity.value
        cachedParticleSize = MotionState.particleSize.value
        isOverlayActive = MotionState.isOverlayActive.value
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postOnAnimation(tickRunnable)

        configJob = CoroutineScope(Dispatchers.Main).launch {
            launch {
                MotionState.particleColorTheme.collectLatest { theme ->
                    updateThemeColors(theme)
                    regenerateParticles()
                }
            }
            launch {
                MotionState.baseParticlesPerSide.collectLatest { count ->
                    cachedBaseCount = count
                    regenerateParticles()
                }
            }
            launch {
                MotionState.gyroSensitivity.collectLatest { s ->
                    cachedSensitivity = s
                }
            }
            launch {
                MotionState.particleSize.collectLatest { s ->
                    cachedParticleSize = s
                    regenerateParticles()
                }
            }
            launch {
                MotionState.isOverlayActive.collectLatest { active ->
                    isOverlayActive = active
                    if (!active) {
                        synchronized(particles) { particles.clear() }
                    } else {
                        regenerateParticles()
                    }
                    postInvalidate()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(tickRunnable)
        configJob?.cancel()
        configJob = null
    }

    fun updateGyroData(gx: Float, gy: Float, gz: Float) {
        targetGyroX = gx
        targetGyroY = gy
        targetGyroZ = gz
    }

    private fun updateThemeColors(themeName: String) {
        cachedTheme = themeName
        activeColors = when (themeName) {
            "Deep Ocean" -> intArrayOf(0xFF1565C0.toInt(), 0xFF1E88E5.toInt(), 0xFF42A5F5.toInt(), 0xFF90CAF9.toInt(), 0xFF0D47A1.toInt())
            "Soft Lavender" -> intArrayOf(0xFF7E57C2.toInt(), 0xFF9575CD.toInt(), 0xFFB39DDB.toInt(), 0xFFD1C4E9.toInt(), 0xFF5E35B1.toInt())
            "Sunset Amber" -> intArrayOf(0xFFFFA726.toInt(), 0xFFFFB74D.toInt(), 0xFFFFCC80.toInt(), 0xFFFFE082.toInt(), 0xFFF57C00.toInt())
            else -> intArrayOf(0xFF00ACC1.toInt(), 0xFF00897B.toInt(), 0xFF4DB6AC.toInt(), 0xFF80CBC4.toInt(), 0xFF006064.toInt())
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        regenerateParticles()
    }

    private fun regenerateParticles() {
        if (width <= 0 || height <= 0 || !isOverlayActive) return
        synchronized(particles) {
            particles.clear()
            val scale = resources.displayMetrics.density
            val margin = 40f * scale // More margin for top/bottom
            val sideMargin = 16f * scale
            val staggeredOffset = 12f * scale // Zigzag depth
            val availableHeight = height - 2 * margin
            val spacing = availableHeight / (cachedBaseCount + 1)

            for (side in 0..1) { // 0 = Left, 1 = Right
                val baseHomeX = if (side == 0) sideMargin else width - sideMargin
                for (i in 1..cachedBaseCount) {
                    val homeY = margin + i * spacing
                    // Zigzag pattern: offset every other dot
                    val xOffset = if (i % 2 == 0) staggeredOffset else -staggeredOffset
                    val homeX = if (side == 0) baseHomeX + xOffset else baseHomeX - xOffset
                    
                    val r = cachedParticleSize * scale
                    val color = activeColors[random.nextInt(activeColors.size)]
                    
                    particles.add(
                        Particle(
                            homeX = homeX,
                            homeY = homeY,
                            x = homeX,
                            y = homeY,
                            vx = 0f,
                            vy = 0f,
                            radius = r,
                            color = color,
                            alpha = 0.8f,
                            alphaOffset = random.nextFloat() * 10f,
                            state = ParticleState.ACTIVE
                        )
                    )
                }
            }
        }
    }

    private fun updatePhysics() {
        if (width <= 0 || height <= 0) return
        frameTime += 0.05f
        synchronized(particles) {
            if (particles.isEmpty()) return

            // Smoother gyro smoothing
            gyroX = gyroX * 0.92f + targetGyroX * 0.08f
            gyroY = gyroY * 0.92f + targetGyroY * 0.08f
            gyroZ = gyroZ * 0.92f + targetGyroZ * 0.08f

            val motionMagnitude = sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ)
            val threshold = 0.02f
            val isMoving = motionMagnitude > threshold

            val scale = resources.displayMetrics.density
            val maxScatter = 45f * scale * cachedSensitivity
            val friction = 0.92f // Increased friction for more control
            val returnSpeed = 0.04f
            val fadeSpeed = 0.15f

            // Dynamic count logic: show extra particles if motion is high
            val extraCountTarget = if (motionMagnitude > 1.2f) 3 else if (motionMagnitude > 0.6f) 1 else 0
            val currentExtras = particles.count { it.isExtra }
            
            if (currentExtras < extraCountTarget) {
                val side = random.nextInt(2)
                val baseHomeX = if (side == 0) 16f * scale else width - 16f * scale
                val xOffset = (random.nextFloat() - 0.5f) * 20f * scale
                val homeY = random.nextFloat() * height
                particles.add(
                    Particle(
                        homeX = baseHomeX + xOffset, homeY = homeY, x = baseHomeX + xOffset, y = homeY,
                        vx = 0f, vy = 0f, radius = cachedParticleSize * scale * 0.8f,
                        color = activeColors[random.nextInt(activeColors.size)],
                        alpha = 0f, alphaOffset = random.nextFloat() * 10f, isExtra = true,
                        state = ParticleState.FADING_IN
                    )
                )
            }

            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                
                if (isMoving) {
                    // Motion logic
                    val forceX = -gyroY * cachedSensitivity * 1.5f * scale // Reduced multiplier
                    val forceY = gyroX * cachedSensitivity * 1.5f * scale
                    
                    p.vx += forceX + (random.nextFloat() - 0.5f) * 0.1f * scale
                    p.vy += forceY + (random.nextFloat() - 0.5f) * 0.1f * scale
                    
                    p.vx *= friction
                    p.vy *= friction
                    
                    p.x += p.vx
                    p.y += p.vy
                    
                    val dx = p.x - p.homeX
                    val dy = p.y - p.homeY
                    val dist = sqrt(dx * dx + dy * dy)

                    // Recycling Logic
                    if (dist > maxScatter && p.state == ParticleState.ACTIVE) {
                        p.state = ParticleState.FADING_OUT
                    }

                    when (p.state) {
                        ParticleState.FADING_OUT -> {
                            p.alpha -= fadeSpeed
                            if (p.alpha <= 0f) {
                                p.alpha = 0f
                                // Respawn at home
                                p.x = p.homeX
                                p.y = p.homeY
                                p.vx = 0f
                                p.vy = 0f
                                p.state = ParticleState.FADING_IN
                            }
                        }
                        ParticleState.FADING_IN -> {
                            p.alpha += fadeSpeed
                            if (p.alpha >= 0.8f) {
                                p.alpha = 0.8f
                                p.state = ParticleState.ACTIVE
                            }
                        }
                        ParticleState.ACTIVE -> {
                            val targetAlpha = 0.4f + 0.5f * (0.5f + 0.5f * sin((frameTime + p.alphaOffset).toDouble()).toFloat())
                            p.alpha = p.alpha * 0.8f + targetAlpha * 0.2f
                        }
                    }
                } else {
                    // Return home logic
                    p.vx = 0f
                    p.vy = 0f
                    p.x = p.x * (1 - returnSpeed) + p.homeX * returnSpeed
                    p.y = p.y * (1 - returnSpeed) + p.homeY * returnSpeed
                    
                    val targetAlpha = if (p.isExtra) 0f else 0.8f
                    p.alpha = p.alpha * 0.9f + targetAlpha * 0.1f
                    
                    if (p.alpha < 0.1f) {
                        if (p.isExtra) {
                            iterator.remove()
                            continue
                        } else {
                            p.state = ParticleState.ACTIVE // Reset state for base particles
                        }
                    } else if (p.alpha > 0.7f && !p.isExtra) {
                        p.state = ParticleState.ACTIVE
                    }
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isOverlayActive) return
        synchronized(particles) {
            for (p in particles) {
                if (p.alpha <= 0.01f) continue
                paint.color = p.color
                paint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(p.x, p.y, p.radius, paint)
            }
        }
    }
}
