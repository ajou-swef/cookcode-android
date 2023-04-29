package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.ActivityRecipePreviewBinding

class RecipePreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // step 정보 담아두는 변수
        val datas = mutableListOf<StepData>()

        // 스텝의 개수
        val steps = intent.getIntExtra("index", 0)

        // 각 스텝 별로 step data 저장
        datas.apply {
            for(i: Int in 0 until steps) {
                val stepNumber = intent.getIntExtra("step_number$i", 0)
                val stepImages = intent.getStringArrayExtra("images$i")!!.toList()
                val stepVideos = intent.getStringArrayExtra("videos$i")?.toList()
                val stepTitle = intent.getStringExtra("title$i")!!
                val stepDescription = intent.getStringExtra("description$i")!!

                add(StepData(stepImages, stepVideos, stepTitle, stepDescription, stepNumber))
            }
        }

        Log.d("data_size", "size $datas")

    }
}