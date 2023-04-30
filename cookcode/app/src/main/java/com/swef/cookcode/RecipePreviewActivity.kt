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

        // page 이동시 양 옆에 이전 스텝이 보이도록 하기
        val offsetBetweenPages = resources.getDimensionPixelOffset(R.dimen.offsetBetweenPages).toFloat()
        binding.viewpager.setPageTransformer { page, position ->
            val myOffset = position * -(2 * offsetBetweenPages)
            if (position < -1) {
                page.translationX = -myOffset
            } else if (position <= 1) {
                // Paging 시 Y축 Animation 배경색을 약간 연하게 처리
                val scaleFactor = 0.8f.coerceAtLeast(1 - kotlin.math.abs(position))
                page.translationX = myOffset
                page.scaleY = scaleFactor
                page.alpha = scaleFactor
            } else {
                page.alpha = 0f
                page.translationX = myOffset
            }
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