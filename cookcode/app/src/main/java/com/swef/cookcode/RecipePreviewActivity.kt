package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.swef.cookcode.adapter.StepPreviewRecyclerviewAdapter
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.ActivityRecipePreviewBinding

class RecipePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipePreviewBinding

    private lateinit var stepPreviewRecyclerviewAdapter: StepPreviewRecyclerviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 보고있는 step
        var currentPosition = 0

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

        stepPreviewRecyclerviewAdapter = StepPreviewRecyclerviewAdapter(datas)
        binding.viewpager.apply {
            adapter = stepPreviewRecyclerviewAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // Paging 완료되면 호출
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                stepPreviewRecyclerviewAdapter.stopCurrentVideo()
            }
        })

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 레시피 업로드
        binding.btnUpload.setOnClickListener {

        }

        // 미리보기 단계에서 수정
        binding.editButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("step_number", currentPosition)

            setResult(RESULT_OK, intent)
            finish()
        }

    }
}