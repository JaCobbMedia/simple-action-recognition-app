package com.jacobb.simpleactionrecognition.utils

import android.content.Context
import android.graphics.Bitmap
import com.jacobb.simpleactionrecognition.classifiers.Person
import com.jacobb.simpleactionrecognition.classifiers.PoseNetPoseClassifier

class PoseAnalyzer(context: Context) {

    private var poseClassifier = PoseNetPoseClassifier(context)
    val modelWidth = poseClassifier.modelWidth
    val modelHeight = poseClassifier.modelHeight
    val lastInferenceTime: Long
        get() = poseClassifier.lastInferenceTimeNanos

    fun analyze(bitmap: Bitmap): Person {
        return poseClassifier.estimateSinglePose(bitmap)
    }
}
