package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.databinding.ActivityInfluencerManageBinding

class InfluencerManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfluencerManageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfluencerManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.beforeArrow.setOnClickListener {
            finish()
        }
    }
}