package com.swef.cookcode.data

import android.net.Uri
import com.swef.cookcode.data.response.MadeUser

data class RecipeData(
    val recipeId : Int,
    val title: String,
    val description: String,
    val mainImage: Uri,
    val likes: Int,
    // val views: Int,
    val madeUser: MadeUser
)

data class RecipeAndStepData(
    val recipeData: RecipeData,
    val stepData: List<StepData>
)
