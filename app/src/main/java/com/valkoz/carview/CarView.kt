package com.valkoz.carview

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.math.*

class CarView: View {

    private var isCarMoving: Boolean = false
    private var carPositionX: Float = 0f
    private var carPositionY: Float = 0f
    private var carRotation: Float = 0f

    private val bitmapMatrix = Matrix()

    private lateinit var carBitmap: Bitmap

    private var clickPositionX: Float = 0f
    private var clickPositionY: Float = 0f

    private lateinit var path: Path

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        path = Path()
        carBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_car)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (it.action == MotionEvent.ACTION_DOWN) {

                if (!isCarMoving) {

                    clickPositionX = event.x
                    clickPositionY = event.y

                    if (clickPositionX == carPositionX && clickPositionY == carPositionY)
                        return true

                    isCarMoving = true

                    var currentCarRadians = Math.toRadians(carRotation.toDouble())
                    var newCarRadians = atan2(
                        (clickPositionX - carPositionX).toDouble(),
                        (-(clickPositionY - carPositionY)).toDouble()
                    )

                    if (abs(currentCarRadians - newCarRadians) > Math.PI) {
                        when {
                            newCarRadians < 0 -> newCarRadians += Math.PI.toFloat() * 2
                            currentCarRadians < 0 -> currentCarRadians += Math.PI.toFloat() * 2
                            currentCarRadians > Math.PI -> currentCarRadians -= Math.PI.toFloat() * 2
                        }
                    }

                    val currentCarRotation = Math.toDegrees(currentCarRadians).toFloat()
                    val newCarRotation = Math.toDegrees(newCarRadians).toFloat()

                    val rotation = ValueAnimator.ofFloat(currentCarRotation, newCarRotation).apply {
                        duration = 1000
                        addUpdateListener {
                            carRotation = animatedValue as Float
                            postInvalidateOnAnimation()
                        }
                    }

                    path.reset()
                    path.moveTo(carPositionX, carPositionY)
                    path.lineTo(clickPositionX, clickPositionY)

                    val moving = ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 2000
                        addUpdateListener {
                            val point = arrayOf(0.0f, 0.0f).toFloatArray()
                            val pathMeasure = PathMeasure(path, false)
                            pathMeasure.getPosTan(pathMeasure.length * animatedFraction, point, null)
                            carPositionX = point[0]
                            carPositionY = point[1]
                            postInvalidateOnAnimation()
                        }

                    }

                    AnimatorSet().also {
                        it.playSequentially(rotation, moving)
                        it.start()
                        it.doOnEnd {
                            isCarMoving = false
                        }
                    }
                }
                return true
            }
        }
        return false
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        carPositionX = width / 2f
        carPositionY = height - (height / 6f)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bitmapMatrix.reset()
        bitmapMatrix.setRotate(carRotation, carBitmap.width / 2f, carBitmap.height / 2f)
        bitmapMatrix.postTranslate(carPositionX - carBitmap.width / 2f, carPositionY - carBitmap.height / 2f)

        canvas.drawBitmap(carBitmap, bitmapMatrix, null)
    }

}