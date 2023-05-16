package com.swef.cookcode.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.RecipeActivity
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.databinding.SearchRecipeRecyclerviewItemBinding

class SearchRecipeRecyclerviewAdapter(
    private val context: Context
): RecyclerView.Adapter<SearchRecipeRecyclerviewAdapter.ViewHolder>() {
    private lateinit var binding: SearchRecipeRecyclerviewItemBinding

    private val ERR_USER_CODE = -1

    var datas = mutableListOf<RecipeAndStepData>()
    var userId = ERR_USER_CODE

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
            getImageFromUrl(item.recipeData.mainImage, binding.mainImage)

            binding.layout.setOnClickListener {
                val intent = Intent(binding.layout.context, RecipeActivity::class.java)
                intent.putExtra("recipe_id", item.recipeData.recipeId)
                intent.putExtra("user_id", userId)
                intent.putExtra("access_token", accessToken)
                binding.layout.context.startActivity(intent)
            }
        }
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .into(view)
    }
}