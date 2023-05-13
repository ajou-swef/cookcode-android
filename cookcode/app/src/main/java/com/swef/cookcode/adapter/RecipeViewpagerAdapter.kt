package com.swef.cookcode.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData

class RecipeViewpagerAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var recipeData: RecipeData
    lateinit var stepDatas: List<StepData>

    private lateinit var recipeTitleAdapter: RecipePreviewAdapter
    private lateinit var recipeStepAdapter: StepPreviewRecyclerviewAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            recipeTitleAdapter = RecipePreviewAdapter(recipeData)
            return recipeTitleAdapter.onCreateViewHolder(parent, viewType)
        } else {
            recipeStepAdapter = StepPreviewRecyclerviewAdapter(stepDatas)
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