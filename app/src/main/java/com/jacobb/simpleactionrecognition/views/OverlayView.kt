package com.jacobb.simpleactionrecognition.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.jacobb.simpleactionrecognition.classifiers.KeyPoint
import com.jacobb.simpleactionrecognition.classifiers.Person
import com.jacobb.simpleactionrecognition.classifiers.PoseClassifier.Companion.CONFIDENCE_THRESHOLD
import com.jacobb.simpleactionrecognition.classifiers.PoseClassifier.Companion.joints
import com.jacobb.simpleactionrecognition.utils.dip
import com.jacobb.simpleactionrecognition.utils.sp
import java.util.*

class OverlayView(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {
    private var mRatioWidth = 0
    private var mRatioHeight = 0

    private val mKeyPoints = ArrayList<KeyPoint>()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mRatioX: Float = 0.toFloat()
    private var mRatioY: Float = 0.toFloat()
    private var mImgWidth: Int = 0
    private var mImgHeight: Int = 0

    private val circleRadius: Float by lazy {
        dip(3).toFloat()
    }

    private val mPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            style = Paint.Style.FILL
            strokeWidth = dip(2).toFloat()
            textSize = sp(13).toFloat()
        }
    }

    fun setImgSize(
        width: Int,
        height: Int
    ) {
        mImgWidth = width
        mImgHeight = height
        requestLayout()
    }

    fun setDrawPoint(
        person: Person
    ) {
        mKeyPoints.clear()
        mKeyPoints.addAll(person.keyPoints)
    }

    fun setAspectRatio(
        width: Int,
        height: Int
    ) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Size cannot be negative.")
        }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mKeyPoints.isEmpty()) return
        mPaint.color = Color.RED
        for (keyPoint in mKeyPoints) {
            if (keyPoint.score > CONFIDENCE_THRESHOLD) {
                val tempX = keyPoint.position.x / mRatioX
                val tempY = keyPoint.position.y / mRatioY
                canvas.drawCircle(tempX, tempY, circleRadius, mPaint)
            }
        }
        mPaint.color = Color.WHITE
        for (joint in joints) {
            if (mKeyPoints[joint.first.ordinal].score > CONFIDENCE_THRESHOLD &&
                mKeyPoints[joint.second.ordinal].score > CONFIDENCE_THRESHOLD) {
                val tempX1 = mKeyPoints[joint.first.ordinal].position.x / mRatioX
                val tempY1 = mKeyPoints[joint.first.ordinal].position.y / mRatioY
                val tempX2 = mKeyPoints[joint.second.ordinal].position.x / mRatioX
                val tempY2 = mKeyPoints[joint.second.ordinal].position.y / mRatioY
                canvas.drawLine(tempX1, tempY1, tempX2, tempY2, mPaint)
            }
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                mWidth = width
                mHeight = width * mRatioHeight / mRatioWidth
            } else {
                mWidth = height * mRatioWidth / mRatioHeight
                mHeight = height
            }
        }

        setMeasuredDimension(mWidth, mHeight)

        mRatioX = mImgWidth.toFloat() / mWidth
        mRatioY = mImgHeight.toFloat() / mHeight
    }
}