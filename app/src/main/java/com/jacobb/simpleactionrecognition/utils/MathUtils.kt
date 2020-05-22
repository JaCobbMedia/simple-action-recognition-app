package com.jacobb.simpleactionrecognition.utils

import android.graphics.Point
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtils{
    fun calcRotationAngleInDegrees(
        centerPt: Point,
        targetPt: Point
    ): Double {
        var theta = atan2(
            targetPt.y - centerPt.y.toDouble(),
            targetPt.x - centerPt.x.toDouble()
        )
        theta += Math.PI / 2.0
        var angle = Math.toDegrees(theta)

        if (angle < 0) {
            angle += 360.0
        }
        return angle
    }

    fun calculateDistance(point1: Point, point2: Point): Double {
        return sqrt((point2.x - point1.x.toDouble()).pow(2.0) +
                    (point2.y - point1.y.toDouble()).pow(2.0) * 1.0)
    }

    fun velocity(points: List<MovementPoint>): Float {
        return if (points.isEmpty() || points.size == 1 ) {
            0f
        } else {
            var totalDistance: Float = 0f
            var totalTime: Float = 0f
            for (i in 0 until points.size-1) {
                totalDistance += calculateDistance(
                    Point(points[i].x, points[i].y),
                    Point(points[i+1].x, points[i+1].y)
                ).toFloat()
                totalTime += points[i].elapsedTime
            }
            totalDistance / totalTime
        }
    }

    fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    data class MovementPoint(
        val x: Int,
        val y: Int,
        val elapsedTime: Long
    )
}