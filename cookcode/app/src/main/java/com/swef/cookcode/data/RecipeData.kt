package com.swef.cookcode.data

import com.swef.cookcode.data.response.MadeUser
data class RecipeData(
    val recipeId : Int,
    val title: String,
    val description: String,
    val mainImage: String,
    val likes: Int,
    val cookable: Boolean,
    val madeUser: MadeUser
)

data class RecipeAndStepData(
    val recipeData: RecipeData,
    val stepData: List<StepData>
)
