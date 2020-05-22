package com.jacobb.simpleactionrecognition.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import com.jacobb.simpleactionrecognition.utils.PoseAnalyzer

abstract class BasePoseAnalyzerViewModel(context: Context) : BaseObservable() {

    val poseAnalyzer = PoseAnalyzer(context)

    val messageTextField1 = ObservableField<String>()
    val messageTextField2 = ObservableField<String>()
    val messageTextField3 = ObservableField<String>()

    abstract fun analyzePose(bitmap: Bitmap)
}