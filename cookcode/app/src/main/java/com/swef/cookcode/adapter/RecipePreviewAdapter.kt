package com.swef.cookcode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.host.IngredientDataHost
import com.swef.cookcode.data.response.Ingredient
import com.swef.cookcode.databinding.RecipePreviewItemBinding

class RecipePreviewAdapter(
    recipeData: RecipeData,
    private val context: Context
) : RecyclerView.Adapter<RecipePreviewAdapter.ViewHolder>() {

    var data = recipeData
    private lateinit var binding: RecipePreviewItemBinding

    private lateinit var essentialIngredientsRecyclerviewAdapter: IngredientRecyclerviewAdapter
    private lateinit var additionalIngredientsRecyclerviewAdapter: IngredientRecyclerviewAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = RecipePreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            getImageFromUrl(item.mainImage)
            binding.recipeName.text = item.title
            binding.madeUser.text = item.madeUser.nickname
            binding.likeNumber.text = item.likes.toString()
            binding.descriptionText.text = item.description

            // 필수재료, 추가재료 확인
            essentialIngredientsRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe_preview")
            essentialIngredientsRecyclerviewAdapter.filteredDatas =
                makeIngredientToMyIngredientData(item.ingredients) as MutableList<MyIngredientData>
            binding.essentialIngredients.adapter = essentialIngredientsRecyclerviewAdapter
            binding.essentialIngredients.layoutManager = LinearLayoutManagerWrapper(
                context, LinearLayoutManager.HORIZONTAL, false
            )

            additionalIngredientsRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe_preview")
            additionalIngredientsRecyclerviewAdapter.filteredDatas =
                makeIngredientToMyIngredientData(item.additionalIngredients) as MutableList<MyIngredientData>
            binding.additionalIngredients.adapter = additionalIngredientsRecyclerviewAdapter
            binding.additionalIngredients.layoutManager = LinearLayoutManagerWrapper(
                context, LinearLayoutManager.HORIZONTAL, false
            )

            if (item.additionalIngredients.isEmpty()) {
                binding.additionalIngredients.visibility = View.GONE
                binding.divider.visibility = View.GONE
            }
        }
    }

    private fun getImageFromUrl(imageUrl: String) {
        Glide.with(context)
            .load(imageUrl)
            .into(binding.mainImage)
    }

    private fun makeIngredientToMyIngredientData(ingredients: List<Ingredient>): List<MyIngredientData> {
        val myIngredientDatas = mutableListOf<MyIngredientData>()
        for(item in ingredients) {
            val ingredientData = IngredientDataHost().getIngredientFromId(item.ingredId)
            if (ingredientData != null) {
                ingredientData.value = 100
            }
            myIngredientDatas.add(ingredientData!!)
        }

        return myIngredientDatas
    }
}

class LinearLayoutManagerWrapper(context: Context, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}