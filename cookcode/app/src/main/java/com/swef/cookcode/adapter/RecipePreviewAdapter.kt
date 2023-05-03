package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.databinding.RecipePreviewItemBinding

class RecipePreviewAdapter(
    recipeData: RecipeData
) : RecyclerView.Adapter<RecipePreviewAdapter.ViewHolder>() {

    var data = recipeData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecipePreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data)
    }

    inner class ViewHolder(
        private val binding: RecipePreviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecipeData){
            binding.mainImage.setImageURI(item.mainImage)
            binding.recipeName.text = item.title
            binding.madeUser.text = item.madeUser
            binding.likeNumber.text = item.likes.toString()
            binding.viewNumber.text = item.views.toString()
            binding.descriptionText.text = item.description

            // 필수재료, 추가재료 확인
            binding.essentialIngredients.visibility = View.GONE
            binding.additionalIngredients.visibility = View.GONE
        }
    }


}