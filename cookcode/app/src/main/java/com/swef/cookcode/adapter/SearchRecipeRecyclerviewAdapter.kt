package com.swef.cookcode.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.databinding.SearchRecipeRecyclerviewItemBinding

class SearchRecipeRecyclerviewAdapter(
): RecyclerView.Adapter<SearchRecipeRecyclerviewAdapter.ViewHolder>() {
    private lateinit var binding: SearchRecipeRecyclerviewItemBinding

    var datas = mutableListOf<RecipeData>()

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
        holder.bind(datas[position], position)
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: SearchRecipeRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecipeData, position: Int){
            binding.recipeName.text = item.title
            binding.viewNumber.text = item.views.toString()
            binding.likeNumber.text = item.likes.toString()
            binding.madeUser.text = item.madeUser
            binding.mainImage.setImageURI(item.mainImage)

            binding.layout.setOnClickListener {
                // recipe 보여주기
                Log.d("data_click", position.toString()+"recyclerview clicked")
            }
        }
    }
}