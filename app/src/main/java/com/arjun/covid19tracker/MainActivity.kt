package com.arjun.covid19tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arjun.covid19tracker.databinding.ActivityMainBinding
import com.arjun.covid19tracker.util.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}