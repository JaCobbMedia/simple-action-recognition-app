package com.jacobb.simpleactionrecognition.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import androidx.databinding.ObservableField
import com.jacobb.simpleactionrecognition.classifiers.BodyPartCOCO
import com.jacobb.simpleactionrecognition.classifiers.KeyPoint
import com.jacobb.simpleactionrecognition.classifiers.Person
import com.jacobb.simpleactionrecognition.classifiers.PoseClassifier.Companion.CONFIDENCE_THRESHOLD
import com.jacobb.simpleactionrecognition.utils.HumanActions
import com.jacobb.simpleactionrecognition.utils.MathUtils

class HandGestureRecognitionViewModel(context: Context) : BasePoseAnalyzerViewModel(context){

    val person = ObservableField<Person>()
    private var leftPalmMovementPoints = ArrayList<MathUtils.MovementPoint>()
    private var rightPalmMovementPoints = ArrayList<MathUtils.MovementPoint>()

    override fun analyzePose(bitmap: Bitmap) {
        val analyzedPerson = poseAnalyzer.analyze(bitmap)
        this.person.set(analyzedPerson)
        predictAction(analyzedPerson.keyPoints)
        predictWaving(analyzedPerson.keyPoints, poseAnalyzer.lastInferenceTime)
        messageTextField1.set("Prediction score: ${analyzedPerson.score}")
        messageTextField2.set("Prediction time: ${poseAnalyzer.lastInferenceTime} ms")
    }

    private fun predictWaving(keyPoints: List<KeyPoint>, inferenceTime: Long) {
        val leftPalm = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.LEFT_WRIST }
        val rightPalm = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.RIGHT_WRIST }
        if (leftPalm.score > CONFIDENCE_THRESHOLD) {
            if(leftPalmMovementPoints.size > 5) {
                val temp = leftPalmMovementPoints.drop(leftPalmMovementPoints.size - 5)
                leftPalmMovementPoints.clear()
                leftPalmMovementPoints.addAll(temp)
            }
            leftPalmMovementPoints.add(
                MathUtils.MovementPoint(
                leftPalm.position.x, leftPalm.position.y, inferenceTime
            ))
            val velocity = MathUtils.velocity(leftPalmMovementPoints)
            if (velocity > VELOCITY_THRESHOLD) {
                messageTextField3.set("Last recognised action: ${HumanActions.WAVING}")
            }
            Log.i("HandActions","Left arm velocity: $velocity")
        } else {
            val temp = leftPalmMovementPoints.drop(1)
            leftPalmMovementPoints.clear()
            leftPalmMovementPoints.addAll(temp)
        }

        if (rightPalm.score > CONFIDENCE_THRESHOLD) {
            if(rightPalmMovementPoints.size > 5) {
                val temp = rightPalmMovementPoints.drop(rightPalmMovementPoints.size - 5)
                rightPalmMovementPoints.clear()
                rightPalmMovementPoints.addAll(temp)
            }
            rightPalmMovementPoints.add(
                MathUtils.MovementPoint(
                    rightPalm.position.x, rightPalm.position.y, inferenceTime
                ))
            val velocity = MathUtils.velocity(rightPalmMovementPoints)
            if (velocity > VELOCITY_THRESHOLD) {
                messageTextField3.set("Last recognised action: ${HumanActions.WAVING}")
            }
            Log.i("HandActions","Right arm velocity: $velocity")
        } else {
            val temp = rightPalmMovementPoints.drop(1)
            rightPalmMovementPoints.clear()
            rightPalmMovementPoints.addAll(temp)
        }
    }

    private fun predictAction(keyPoints: List<KeyPoint>) {
        val leftPalm = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.LEFT_WRIST }
        val rightPalm = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.RIGHT_WRIST }
        val leftShoulder = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.LEFT_SHOULDER }
        val rightShoulder = keyPoints.first { it.bodyPartCOCO == BodyPartCOCO.RIGHT_SHOULDER }

        if (leftPalm.score > CONFIDENCE_THRESHOLD && leftShoulder.score > CONFIDENCE_THRESHOLD) {
            val degrees = MathUtils.calcRotationAngleInDegrees(
                Point(leftShoulder.position.x, leftShoulder.position.y),
                Point(leftPalm.position.x, leftPalm.position.y)
            )
            if (degrees > 270f || degrees < 90f) {
                messageTextField3.set("Last recognised action: ${HumanActions.RIGHT_HAND_UP}") //front camera mirroring
            }
            Log.i("HandActions","Left arm degrees: $degrees")
        }

        if (rightPalm.score > CONFIDENCE_THRESHOLD && rightShoulder.score > CONFIDENCE_THRESHOLD) {
            val degrees = MathUtils.calcRotationAngleInDegrees(
                Point(rightShoulder.position.x, rightShoulder.position.y),
                Point(rightPalm.position.x, rightPalm.position.y)
            )
            if (degrees > 270f || degrees < 90f) {
                messageTextField3.set("Last recognised action: ${HumanActions.LEFT_HAND_UP}") //front camera mirroring
            }
            Log.i("HandActions","Right arm degrees: $degrees")
        }
    }
    companion object {
        private const val VELOCITY_THRESHOLD = 0.15f
    }
}