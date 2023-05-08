package com.swef.cookcode.data

import android.net.Uri
import java.util.Date

data class IngredientData(
    val image: Uri,
    val name: String,
    val type: String,
    val unit: String,
    val ingredId: Int,
)

data class MyIngredientData(
    var ingredientData: IngredientData,
    val fridgeIngredId: Int?,
    var value: Int?,
    var expiredAt: Date?,
    var visibility: Int?
)