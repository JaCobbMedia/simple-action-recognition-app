package com.jacobb.simpleactionrecognition.activities

import android.os.Bundle
import androidx.databinding.Observable
import com.jacobb.simpleactionrecognition.viewmodels.PushupsCounterViewModel

class PushupsCounterActivity: BasePoseAnalyzerActivity() {

    private val viewModel = PushupsCounterViewModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.overlayView.setImgSize(
            viewModel.poseAnalyzer.modelWidth,
            viewModel.poseAnalyzer.modelHeight
        )
        binding.viewmodel = viewModel
        setUpPersonObserver()
    }

    private fun setUpPersonObserver() {
        viewModel.person.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                viewModel.person.get()?.let {
                    runOnUiThread {
                        binding.overlayView.setDrawPoint(it)
                        binding.overlayView.invalidate()
                    }
                }
            }
        })
    }

    override fun classifyNewImage() {
        val bitmap = binding.autoFitTexture.getBitmap(
            viewModel.poseAnalyzer.modelWidth,
            viewModel.poseAnalyzer.modelHeight
        )
        bitmap?.let {
            viewModel.analyzePose(it)
        }
    }
}