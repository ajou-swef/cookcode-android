package com.swef.cookcode.data

import android.net.Uri
import java.util.Date

data class IngredientData(
    val image: Uri,
    val name: String,
    val type: String,
    val ingredId: Int
)

data class MyIngredientData(
    val ingredientData: IngredientData,
    val fridgeIngredId: Int?,
    val value: Int?,
    val expiredAt: Date?
)