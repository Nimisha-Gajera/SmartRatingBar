package com.library.smart_rating_bar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.pow

class SmartRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ===================== CONFIGURABLE PROPERTIES =====================
    var starCount: Int = 5
        private set
    var starSize: Float = 100f
        private set
    var starPadding: Float = 20f
        private set
    private var starFillColor: Int = Color.YELLOW
    private var starEmptyColor: Int = Color.GRAY
    private var animationDuration: Long = 250
    private var animationEnabled: Boolean = true
    private var flyingAnimationEnabled: Boolean = true  // 🆕 New toggle

    // ===================== INTERNAL VARIABLES =====================
    private var rating: Int = 0
    private val starScale = FloatArray(starCount) { 1f }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = starFillColor }
    private val emptyStarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = starEmptyColor }
    private val starPath = Path()

    private val animScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var animationJob: Job? = null

    var ratingChangeListener: OnRatingChangeListener? = null

    private var starFilledDrawable: Drawable? = null
    private var starEmptyDrawable: Drawable? = null

    // 🆕 flying star animation variables
//    private var flyingAlpha = 0f
//    private var flyingOffsetY = 0f
//    private var flyingIndex = -1

    // add this:
//    private var flyingProgress = 0f   // 0..1 progress of the flying animation
    private val flyingStars = mutableListOf<FlyingStar>()

    init {
        clipToOutline = false
//        clipToPadding = false
        context.theme.obtainStyledAttributes(attrs, R.styleable.SmartRatingBar, 0, 0).apply {
            try {
                starCount = getInt(R.styleable.SmartRatingBar_starCount, starCount)
                starSize = getDimension(R.styleable.SmartRatingBar_starSize, starSize)
                starPadding = getDimension(R.styleable.SmartRatingBar_starSpacing, starPadding)
                starFillColor = getColor(R.styleable.SmartRatingBar_starFillColor, starFillColor)
                starEmptyColor = getColor(R.styleable.SmartRatingBar_starEmptyColor, starEmptyColor)
                animationDuration = getInt(
                    R.styleable.SmartRatingBar_bounceAnimationDuration,
                    animationDuration.toInt()
                ).toLong()
                animationEnabled =
                    getBoolean(R.styleable.SmartRatingBar_bounceAnimationEnabled, animationEnabled)
                flyingAnimationEnabled = getBoolean(
                    R.styleable.SmartRatingBar_enableFlyingAnimation,
                    flyingAnimationEnabled
                ) // 🆕 read from XML

                starFilledDrawable = getDrawable(R.styleable.SmartRatingBar_starFilled)
                starEmptyDrawable = getDrawable(R.styleable.SmartRatingBar_starEmpty)
            } finally {
                recycle()
            }
            setPadding(16, 16, 16, 16) // gives room for bounce

        }

        starPaint.color = starFillColor
        emptyStarPaint.color = starEmptyColor
    }

    // ===================== DRAWING =====================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0 until starCount) {
            val left = i * (starSize + starPadding)
            val cx = left + starSize / 2
            val cy = height / 2f
            drawStar(canvas, cx, cy, starSize / 2, i < rating, i)
        }

        // ✨ Draw all flying stars
        if (flyingAnimationEnabled && flyingStars.isNotEmpty()) {
            for (star in flyingStars.toList()) { // copy to avoid modification during iteration
                val i = star.index
                val left = i * (starSize + starPadding)
                val cx = left + starSize / 2f

                val easedProgress = (1f - (1f - star.progress).pow(2))
                val cy = height / 2f - (star.offsetY * easedProgress)

                val fadeStart = 0.3f
                val fadeProgress = ((star.progress - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)
                val startAlpha = 0.9f
                val easedAlpha = (startAlpha * (1f - fadeProgress)).coerceIn(0f, 1f)
                val alphaInt = (easedAlpha * 255f).toInt().coerceIn(0, 255)

                val flyScale = 1f + 0.25f * easedProgress

                val layerPaint = Paint().apply { alpha = alphaInt }
                val layerBounds = RectF(
                    -starSize,
                    -dpToPx(100f),
                    width + starSize,
                    height + starSize
                )
                val layer = canvas.saveLayer(layerBounds, layerPaint)

                canvas.save()
                canvas.scale(flyScale, flyScale, cx, cy)

                val drawable = starFilledDrawable
                if (drawable != null) {
                    drawable.setBounds(
                        (cx - starSize / 2f).toInt(),
                        (cy - starSize / 2f).toInt(),
                        (cx + starSize / 2f).toInt(),
                        (cy + starSize / 2f).toInt()
                    )
                    drawable.draw(canvas)
                } else {
                    val paint = Paint(starPaint)
                    paint.alpha = alphaInt
                    drawStarPath(canvas, cx, cy, starSize / 2f, paint)
                }

                canvas.restore()
                canvas.restoreToCount(layer)
            }
        }


    }

    private fun drawStarPath(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path()
        val angle = Math.PI / 5
        for (i in 0..10) {
            val r = if (i % 2 == 0) radius.toDouble() else radius / 2.5
            val x = (cx + r * Math.sin(i * angle)).toFloat()
            val y = (cy - r * Math.cos(i * angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawStar(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        filled: Boolean,
        index: Int
    ) {
        canvas.save()
        if (index >= 0) canvas.scale(starScale[index], starScale[index], cx, cy)

        val drawable = if (filled) starFilledDrawable else starEmptyDrawable
        if (drawable != null) {
            drawable.setBounds(
                (cx - radius).toInt(),
                (cy - radius).toInt(),
                (cx + radius).toInt(),
                (cy + radius).toInt()
            )
            drawable.draw(canvas)
        } else {
            val paint = if (filled) starPaint else emptyStarPaint
            starPath.reset()
            val angle = Math.PI / 5
            for (i in 0..10) {
                val r = if (i % 2 == 0) radius.toDouble() else radius / 2.5
                val x = (cx + r * Math.sin(i * angle)).toFloat()
                val y = (cy - r * Math.cos(i * angle)).toFloat()
                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
            }
            starPath.close()
            canvas.drawPath(starPath, paint)
        }

        canvas.restore()
    }


    // ===================== ANIMATION =====================
    private fun animateStar(index: Int) {
        if (!animationEnabled) {
            starScale[index] = 1f
            invalidate()
        } else {
            // Bounce animation
            val animator = ValueAnimator.ofFloat(0.8f, 1.4f, 1f)
            animator.duration = animationDuration
            animator.interpolator = OvershootInterpolator()
            animator.addUpdateListener {
                if (index in 0 until starCount) {
                    starScale[index] = it.animatedValue as Float
                    invalidate()
                }
            }
            animator.start()
        }

        // 🆕 Flying star animation
        // 🆕 Flying star animation using flyingProgress (0..1)
        if (flyingAnimationEnabled) {
            val star = FlyingStar(index)
            flyingStars.add(star)

            val flyAnim = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 600L
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val progress = it.animatedValue as Float
                    star.progress = progress
                    star.offsetY = (progress * progress) * dpToPx(70f)
                    invalidate()
                }
                doOnEnd {
                    flyingStars.remove(star)
                    invalidate()
                }
            }
            flyAnim.start()
        }

    }

    private inline fun ValueAnimator.doOnEnd(crossinline action: () -> Unit) {
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) = action()
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    // ===================== USER INTERACTION =====================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.coerceIn(0f, width.toFloat())
        val newRating =
            ceil((x / (starSize + starPadding)).toDouble()).toInt().coerceIn(0, starCount)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> fillStarsSequentially(newRating)
            MotionEvent.ACTION_UP -> if (newRating > 0) animateStar(newRating - 1)
        }
        return true
    }

    private fun fillStarsSequentially(newRating: Int) {
        animationJob?.cancel()
        animationJob = animScope.launch {
            if (newRating > rating) {
                for (i in rating until newRating) {
                    rating = i + 1
                    animateStar(i)       // bounce only current star
                    ratingChangeListener?.onRatingChanged(rating)
                    invalidate()
                    delay(100L)
                }
            } else if (newRating < rating) {
                for (i in (rating - 1) downTo newRating) {
                    rating = i
                    ratingChangeListener?.onRatingChanged(rating)
                    invalidate()         // unfill without bounce
                    delay(80L)
                }
            }
        }
    }

    // ===================== CONFIGURATION FUNCTIONS =====================
    fun setStarCount(count: Int) {
        if (count > 0) {
            starCount = count
            starScale.fill(1f)
            invalidate()
        }
    }

    fun setStarSize(size: Float) {
        Log.e("TAG", "setStarSize: " + size)
        val sizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            size,
            resources.displayMetrics
        )
        starSize = sizePx
        invalidate()
    }

    fun setStarPadding(padding: Float) {
        starPadding = padding
        invalidate()
    }

    fun setStarFillColor(color: Int) {
        starFillColor = color
        starPaint.color = color
        invalidate()
    }

    fun setStarEmptyColor(color: Int) {
        starEmptyColor = color
        emptyStarPaint.color = color
        invalidate()
    }

    fun setAnimationDuration(duration: Long) {
        animationDuration = duration
    }

    fun enableAnimation(enable: Boolean) {
        animationEnabled = enable
    }

    fun setRating(rating: Int) {
        this.rating = rating.coerceIn(0, starCount)
        invalidate()
    }

    fun getRating(): Int = rating

    interface OnRatingChangeListener {
        fun onRatingChanged(rating: Int)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth =
            ((starSize + starPadding) * starCount - starPadding).toInt() + paddingLeft + paddingRight
        val desiredHeight = (starSize + paddingTop + paddingBottom).toInt()
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height =
            resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

}
