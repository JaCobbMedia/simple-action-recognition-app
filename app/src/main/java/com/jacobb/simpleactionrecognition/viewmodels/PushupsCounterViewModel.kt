package com.jacobb.simpleactionrecognition.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import androidx.databinding.ObservableField
import com.jacobb.simpleactionrecognition.classifiers.BodyPartCOCO
import com.jacobb.simpleactionrecognition.classifiers.KeyPoint
import com.jacobb.simpleactionrecognition.classifiers.Person
import com.jacobb.simpleactionrecognition.classifiers.PoseClassifier
import com.jacobb.simpleactionrecognition.utils.MathUtils

class PushupsCounterViewModel(context: Context): BasePoseAnalyzerViewModel(context) {

    val person = ObservableField<Person>()
    private var wasInPushupState: Boolean = false
    private var pusupsCount: Int = 0

    override fun analyzePose(bitmap: Bitmap) {
        val analyzedPerson = poseAnalyzer.analyze(bitmap)
        person.set(analyzedPerson)
        predictPushUp(analyzedPerson.keyPoints)
    }

    private fun predictPushUp(keyPoints: List<KeyPoint>) {
        val leftElbow = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.LEFT_ELBOW}
        val rightElbow = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.RIGHT_ELBOW }
        val leftShoulder = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.LEFT_SHOULDER }
        val rightShoulder = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.RIGHT_SHOULDER }

        if (leftElbow.score > PoseClassifier.CONFIDENCE_THRESHOLD &&
            leftShoulder.score > PoseClassifier.CONFIDENCE_THRESHOLD &&
            rightElbow.score > PoseClassifier.CONFIDENCE_THRESHOLD &&
            rightShoulder.score > PoseClassifier.CONFIDENCE_THRESHOLD) {

            val degreesLeft = MathUtils.calcRotationAngleInDegrees(
                Point(leftShoulder.position.x, leftShoulder.position.y),
                Point(leftElbow.position.x, leftElbow.position.y)
            )
            val degreesRight = MathUtils.calcRotationAngleInDegrees(
                Point(rightShoulder.position.x, rightShoulder.position.y),
                Point(rightElbow.position.x, rightElbow.position.y)
            )

            if (isInDegreeRange(degreesLeft.toFloat(), 90F) &&
                isInDegreeRange(degreesRight.toFloat(), 270f)) {
                if (!wasInPushupState) {
                    pusupsCount++
                    wasInPushupState = true
                }
            } else {
                wasInPushupState = false
            }
            messageTextField1.set("Left side angle: $degreesLeft")
            messageTextField2.set("Right side angle: $degreesRight")
            messageTextField3.set("Push ups count: $pusupsCount")
        }

    }

    private fun isInDegreeRange(currentDegrees: Float, requiredDegrees: Float): Boolean {
        return requiredDegrees + PUSHUP_DEGREE_LEEWAY > currentDegrees &&
                requiredDegrees - PUSHUP_DEGREE_LEEWAY < currentDegrees
    }

    companion object {
        private const val PUSHUP_DEGREE_LEEWAY = 10f
    }

}