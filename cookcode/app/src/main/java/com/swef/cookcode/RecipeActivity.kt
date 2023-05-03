package com.swef.cookcode

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.adapter.RecipePreviewAdapter
import com.swef.cookcode.adapter.StepPreviewRecyclerviewAdapter
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.ActivityRecipeBinding

class RecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeBinding

    // 레시피 mock data
    private val recipeData = mutableListOf<RecipeData>()
    private val stepDatas = mutableListOf<StepData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val position = intent.getIntExtra("position", -1)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // recipe에 들어가는 모든 사진은 drawable/foor_example로 대체
        val res: Resources = resources
        val uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + res.getResourcePackageName(R.drawable.food_example) +
                    '/' + res.getResourceTypeName(R.drawable.food_example) +
                    '/' + res.getResourceEntryName(R.drawable.food_example)
        )

        stepDatas.apply {
            for(i: Int in 0..2) {
                val imageDatas = mutableListOf<String>()
                imageDatas.apply {
                    add(uri.toString())
                }
                add(StepData(imageDatas, null, (i + 1).toString() + "단계", (i+1).toString() + "단계 요리 만들기", i + 1))
            }
        }

        recipeData.apply {
            add(RecipeData("제육볶음", "맛있는 제육볶음", uri, 25, 25, "haeiny"))
            add(RecipeData("무말랭이", "맛있는 무말랭이", uri, 5, 10, "ymei"))
        }

        val recipeTitleAdapter = RecipePreviewAdapter(recipeData[position])
        val recipeStepAdapter = StepPreviewRecyclerviewAdapter(stepDatas)


        binding.viewpager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                if (viewType == 0) {
                    return recipeTitleAdapter.onCreateViewHolder(parent, viewType)
                } else {
                    return recipeStepAdapter.onCreateViewHolder(parent, viewType)
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                if (position == 0) {
                    recipeTitleAdapter.onBindViewHolder(holder as RecipePreviewAdapter.ViewHolder, position)
                } else {
                    recipeStepAdapter.onBindViewHolder(holder as StepPreviewRecyclerviewAdapter.ViewHolder, position - 1)
                }
            }

            override fun getItemCount(): Int {
                return stepDatas.size + 1
            }

            override fun getItemViewType(position: Int): Int {
                return position
            }
        }
    }
}