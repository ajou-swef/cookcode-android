package com.swef.cookcode.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.ShowLoadingViewBinding

class RecipeViewpagerAdapter(
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var recipeData: RecipeData? = null
    private var stepDatas: List<StepData>? = null

    private lateinit var recipeTitleAdapter: RecipePreviewAdapter
    private lateinit var recipeStepAdapter: StepPreviewRecyclerviewAdapter

    private lateinit var loadingViewHolder: LoadingViewHolder

    lateinit var accessToken: String

    companion object {
        const val VIEW_TYPE_LOADING = 0
        const val VIEW_TYPE_RECIPE = 1
        const val VIEW_TYPE_STEP = 2
    }
    fun setData(data: RecipeAndStepData) {
        this.recipeData = data.recipeData
        this.stepDatas = data.stepData

        Log.d("data_size", recipeData.toString())
        Log.d("data_size", stepDatas.toString())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (recipeData != null) {
            if (viewType == VIEW_TYPE_RECIPE) {
                recipeTitleAdapter = RecipePreviewAdapter(recipeData!!, context)
                recipeTitleAdapter.accessToken = accessToken
                return recipeTitleAdapter.onCreateViewHolder(parent, viewType)
            } else {
                recipeStepAdapter = StepPreviewRecyclerviewAdapter(stepDatas!!, context)
                return recipeStepAdapter.onCreateViewHolder(parent, viewType)
            }
        }
        else {
            val binding = ShowLoadingViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            loadingViewHolder = LoadingViewHolder(binding)
            return loadingViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecipePreviewAdapter.ViewHolder -> {
                recipeTitleAdapter.onBindViewHolder(holder, position)
                loadingViewHolder.hideLoading()
            }
            is StepPreviewRecyclerviewAdapter.ViewHolder -> {
                recipeStepAdapter.onBindViewHolder(holder, position - 1)
                loadingViewHolder.hideLoading()
            }
            is LoadingViewHolder -> holder.showLoading()
        }

    }

    override fun getItemCount(): Int {
        return if (recipeData == null) 1
        else stepDatas!!.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (recipeData == null) {
            VIEW_TYPE_LOADING
        } else {
            if (position == 0) { VIEW_TYPE_RECIPE }
            else { VIEW_TYPE_STEP }
        }
    }

    inner class LoadingViewHolder(
        private val binding: ShowLoadingViewBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun showLoading() {
            binding.loadingMsg.visibility = View.VISIBLE
        }

        fun hideLoading() {
            binding.loadingMsg.visibility = View.GONE
        }
    }
}