package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.adapter.RecipeViewpagerAdapter
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.ActivityRecipeBinding

class RecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeBinding

    // 레시피 mock data
    private lateinit var recipeData: RecipeData
    private var stepDatas = mutableListOf<StepData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        val recipeViewpagerAdapter = RecipeViewpagerAdapter()

        binding.viewpager.adapter = recipeViewpagerAdapter
    }
}