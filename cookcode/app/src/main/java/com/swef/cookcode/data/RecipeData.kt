package com.swef.cookcode.data

import android.net.Uri

data class RecipeData(
    val title: String,
    val description: String,
    val mainImage: Uri,
    val likes: Int,
    val views: Int,
    val madeUser: String
)
