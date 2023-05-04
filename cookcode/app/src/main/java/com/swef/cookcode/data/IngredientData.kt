package com.swef.cookcode.data

import android.net.Uri

data class IngredientData(
    val image: Uri,
    val name: String,
    val value: Int?,
    val type: String,
    val ingredId: Int
)

data class MyIngredientData(
    val ingredientData: IngredientData,
    val fridgeIngredId: Int,
    val value: Int
)