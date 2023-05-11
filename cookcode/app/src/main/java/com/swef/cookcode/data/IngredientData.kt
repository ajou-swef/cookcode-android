package com.swef.cookcode.data

import android.net.Uri

data class IngredientData(
    val image: Uri,
    val name: String,
    val type: String,
    val unit: String,
    val ingredId: Int,
)

data class MyIngredientData(
    var ingredientData: IngredientData,
    var fridgeIngredId: Int?,
    var value: Int?,
    var expiredAt: String?,
    var visibility: Int?
)