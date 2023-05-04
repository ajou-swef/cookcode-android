package com.swef.cookcode.data

import android.net.Uri

data class IngredientData(
    val image: Uri,
    val name: String,
    var value: Int?,
    val type: String
)