package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class StatusResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int
)

data class FridgeIdResponse(
    @SerializedName("fridgeIngredId") val fridgeIngredId: Int
)
data class FridgeResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: FridgeIdResponse
)

data class RecipeIdResponse(
    @SerializedName("recipeId") val recipeId: Int
)

data class RecipeStatusResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: RecipeIdResponse
)
