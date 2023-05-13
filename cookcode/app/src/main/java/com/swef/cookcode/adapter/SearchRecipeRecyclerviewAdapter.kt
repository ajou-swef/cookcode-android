package com.swef.cookcode.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.RecipeActivity
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.databinding.SearchRecipeRecyclerviewItemBinding

class SearchRecipeRecyclerviewAdapter(
): RecyclerView.Adapter<SearchRecipeRecyclerviewAdapter.ViewHolder>() {
    private lateinit var binding: SearchRecipeRecyclerviewItemBinding

    var datas = mutableListOf<RecipeAndStepData>()
    lateinit var accessToken: String

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchRecipeRecyclerviewAdapter.ViewHolder {
        binding = SearchRecipeRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SearchRecipeRecyclerviewAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: SearchRecipeRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecipeAndStepData){
            binding.recipeName.text = item.recipeData.title
            // binding.viewNumber.text = item.views.toString()
            binding.likeNumber.text = item.recipeData.likes.toString()
            binding.madeUser.text = item.recipeData.madeUser.nickname
            binding.mainImage.setImageURI(item.recipeData.mainImage)

            binding.layout.setOnClickListener {
                val intent = Intent(binding.layout.context, RecipeActivity::class.java)
                intent.putExtra("recipe_id", item.recipeData.recipeId)
                intent.putExtra("access_token", accessToken)
                binding.layout.context.startActivity(intent)
            }
        }
    }
}