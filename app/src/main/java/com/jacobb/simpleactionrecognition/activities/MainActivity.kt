package com.jacobb.simpleactionrecognition.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jacobb.simpleactionrecognition.R
import com.jacobb.simpleactionrecognition.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUpButtons()
    }

    private fun setUpButtons() {
        binding.handGestureButton.setOnClickListener {
            val intent = Intent(this, HandGestureRecognitionActivity::class.java)
            startActivity(intent)
        }

        binding.pushupsButton.setOnClickListener {
            val intent = Intent(this, PushupsCounterActivity::class.java)
            startActivity(intent)
        }
    }
}
