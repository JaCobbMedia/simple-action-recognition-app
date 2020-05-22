package com.jacobb.simpleactionrecognition.classifiers

import android.content.Context
import android.graphics.Bitmap
import com.jacobb.simpleactionrecognition.utils.MathUtils.sigmoid
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PoseNetPoseClassifier(
    context: Context
) : PoseClassifier(context,
    filename = "posenet_mobilenet_v1_100_257x257_multi_kpt_stripped.tflite",
    device = Device.GPU
) {
    override val modelWidth: Int
        get() = 257
    override val modelHeight: Int
        get() = 257

    override fun initInputArray(bitmap: Bitmap): ByteBuffer {
        val bytesPerChannel = 4
        val inputChannels = 3
        val batchSize = 1
        val inputBuffer = ByteBuffer.allocateDirect(
            batchSize * bytesPerChannel * bitmap.height * bitmap.width * inputChannels
        )
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()

        val mean = 128.0f
        val std = 128.0f
        for (row in 0 until bitmap.height) {
            for (col in 0 until bitmap.width) {
                val pixelValue = bitmap.getPixel(col, row)
                inputBuffer.putFloat(((pixelValue shr 16 and 0xFF) - mean) / std)
                inputBuffer.putFloat(((pixelValue shr 8 and 0xFF) - mean) / std)
                inputBuffer.putFloat(((pixelValue and 0xFF) - mean) / std)
            }
        }
        return inputBuffer
    }

    override fun initOutputMap(interpreter: Interpreter): HashMap<Int, Any> {
        val outputMap = HashMap<Int, Any>()

        val heatMapsShape = interpreter.getOutputTensor(0).shape()
        outputMap[0] = Array(heatMapsShape[0]) {
            Array(heatMapsShape[1]) {
                Array(heatMapsShape[2]) { FloatArray(heatMapsShape[3]) }
            }
        }

        val offsetsShape = interpreter.getOutputTensor(1).shape()
        outputMap[1] = Array(offsetsShape[0]) {
            Array(offsetsShape[1]) { Array(offsetsShape[2]) { FloatArray(offsetsShape[3]) } }
        }

        val displacementsFwdShape = interpreter.getOutputTensor(2).shape()
        outputMap[2] = Array(offsetsShape[0]) {
            Array(displacementsFwdShape[1]) {
                Array(displacementsFwdShape[2]) { FloatArray(displacementsFwdShape[3]) }
            }
        }

        val displacementsBwdShape = interpreter.getOutputTensor(3).shape()
        outputMap[3] = Array(displacementsBwdShape[0]) {
            Array(displacementsBwdShape[1]) {
                Array(displacementsBwdShape[2]) { FloatArray(displacementsBwdShape[3]) }
            }
        }

        return outputMap
    }

    override fun estimateSinglePose(bitmap: Bitmap): Person {
        val inputArray = arrayOf(initInputArray(bitmap))
        val outputMap = initOutputMap(getInterpreter())
        val inferenceStartTime = System.currentTimeMillis()
        getInterpreter().runForMultipleInputsOutputs(inputArray, outputMap)
        lastInferenceTimeNanos = System.currentTimeMillis() - inferenceStartTime
        return constructPersonModelFromHeatMaps(bitmap, outputMap)
    }

    private fun constructPersonModelFromHeatMaps(
        bitmap: Bitmap,
        outputMap: HashMap<Int, Any>
    ): Person {
        val heatMaps = outputMap[0] as Array<Array<Array<FloatArray>>>
        val offsets = outputMap[1] as Array<Array<Array<FloatArray>>>

        val height = heatMaps[0].size
        val width = heatMaps[0][0].size
        val numKeyPoints = heatMaps[0][0][0].size

        val keyPointPositions = Array(numKeyPoints) { Pair(0, 0) }
        for (keypoint in 0 until numKeyPoints) {
            var maxVal = heatMaps[0][0][0][keypoint]
            var maxRow = 0
            var maxCol = 0
            for (row in 0 until height) {
                for (col in 0 until width) {
                    if (heatMaps[0][row][col][keypoint] > maxVal) {
                        maxVal = heatMaps[0][row][col][keypoint]
                        maxRow = row
                        maxCol = col
                    }
                }
            }
            keyPointPositions[keypoint] = Pair(maxRow, maxCol)
        }

        val xCoords = IntArray(numKeyPoints)
        val yCoords = IntArray(numKeyPoints)
        val confidenceScores = FloatArray(numKeyPoints)
        keyPointPositions.forEachIndexed { idx, position ->
            val positionY = keyPointPositions[idx].first
            val positionX = keyPointPositions[idx].second
            yCoords[idx] = (
                    position.first / (height - 1).toFloat() * bitmap.height +
                            offsets[0][positionY][positionX][idx]
                    ).toInt()
            xCoords[idx] = (
                    position.second / (width - 1).toFloat() * bitmap.width +
                            offsets[0][positionY]
                                    [positionX][idx + numKeyPoints]
                    ).toInt()
            confidenceScores[idx] = sigmoid(heatMaps[0][positionY][positionX][idx])
        }

        val person = Person()
        val keyPointList = Array(numKeyPoints) { KeyPoint() }
        var totalScore = 0.0f
        enumValues<BodyPartCOCO>().forEachIndexed { idx, it ->
            keyPointList[idx].bodyPartCOCO = it
            keyPointList[idx].position.x = xCoords[idx]
            keyPointList[idx].position.y = yCoords[idx]
            keyPointList[idx].score = confidenceScores[idx]
            totalScore += confidenceScores[idx]
        }

        person.keyPoints = keyPointList.toList()
        person.score = totalScore / numKeyPoints

        return person
    }
}