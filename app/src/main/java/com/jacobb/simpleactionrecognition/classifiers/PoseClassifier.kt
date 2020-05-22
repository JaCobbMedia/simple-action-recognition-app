package com.jacobb.simpleactionrecognition.classifiers

import android.content.Context
import android.graphics.Bitmap
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import org.tensorflow.lite.gpu.GpuDelegate

enum class BodyPartCOCO {
    NOSE,
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE
}

class Position {
    var x: Int = 0
    var y: Int = 0
}

class KeyPoint {
    var bodyPartCOCO: BodyPartCOCO =
        BodyPartCOCO.NOSE
    var position: Position =
        Position()
    var score: Float = 0.0f
}

class Person {
    var keyPoints = listOf<KeyPoint>()
    var score: Float = 0.0f
}

enum class Device {
    CPU,
    NNAPI,
    GPU
}

abstract class PoseClassifier(
    val context: Context,
    val filename: String = "posenet_mobilenet_v1_100_257x257_multi_kpt_stripped.tflite",
    val device: Device = Device.CPU
) : AutoCloseable {
    var lastInferenceTimeNanos: Long = -1

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    abstract val modelWidth: Int
    abstract val modelHeight: Int

    protected fun getInterpreter(): Interpreter {
        interpreter?.let {
            return it
        }
        val options = Interpreter.Options()
        options.setNumThreads(NUM_LITE_THREADS)
        when (device) {
            Device.CPU -> { }
            Device.GPU -> {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
            }
            Device.NNAPI -> options.setUseNNAPI(true)
        }
        interpreter = Interpreter(loadModelFile(filename, context), options)
        return interpreter!!
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
    }

    abstract fun initInputArray(bitmap: Bitmap): ByteBuffer

    private fun loadModelFile(path: String, context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(path)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        return inputStream.channel.map(
            FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength
        )
    }

    abstract fun initOutputMap(interpreter: Interpreter): HashMap<Int, Any>

    abstract fun estimateSinglePose(bitmap: Bitmap): Person

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.5f
        private const val NUM_LITE_THREADS = 4
        val joints = listOf(
            Pair(BodyPartCOCO.LEFT_WRIST, BodyPartCOCO.LEFT_ELBOW),
            Pair(BodyPartCOCO.LEFT_ELBOW, BodyPartCOCO.LEFT_SHOULDER),
            Pair(BodyPartCOCO.LEFT_SHOULDER, BodyPartCOCO.RIGHT_SHOULDER),
            Pair(BodyPartCOCO.RIGHT_SHOULDER, BodyPartCOCO.RIGHT_ELBOW),
            Pair(BodyPartCOCO.RIGHT_ELBOW, BodyPartCOCO.RIGHT_WRIST),
            Pair(BodyPartCOCO.LEFT_SHOULDER, BodyPartCOCO.LEFT_HIP),
            Pair(BodyPartCOCO.LEFT_HIP, BodyPartCOCO.RIGHT_HIP),
            Pair(BodyPartCOCO.RIGHT_HIP, BodyPartCOCO.RIGHT_SHOULDER),
            Pair(BodyPartCOCO.LEFT_HIP, BodyPartCOCO.LEFT_KNEE),
            Pair(BodyPartCOCO.LEFT_KNEE, BodyPartCOCO.LEFT_ANKLE),
            Pair(BodyPartCOCO.RIGHT_HIP, BodyPartCOCO.RIGHT_KNEE),
            Pair(BodyPartCOCO.RIGHT_KNEE, BodyPartCOCO.RIGHT_ANKLE)
        )
    }
}