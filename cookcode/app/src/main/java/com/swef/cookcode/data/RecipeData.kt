package com.swef.cookcode.data

import com.swef.cookcode.data.response.Ingredient
import com.swef.cookcode.data.response.MadeUser
data class RecipeData(
    val recipeId : Int,
    val title: String,
    val description: String,
    val mainImage: String,
    var likes: Int,
    var isLiked: Boolean,
    val cookable: Boolean,
    val madeUser: MadeUser,
    val createdAt: String?,
    val ingredients: List<Ingredient>,
    val additionalIngredients: List<Ingredient>
)

data class RecipeAndStepData(
    val recipeData: RecipeData,
    val stepData: List<StepData>
)
