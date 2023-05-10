package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class IngredientResponse(
    @SerializedName("fridgeIngredId") val fridgeIngredId: Int,
    @SerializedName("ingredId") val IngredId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("expiredAt") val expiredAt: String,
    @SerializedName("category") val type: String,
    @SerializedName("quantity") val value: Int,
)

data class IngredientListResponse(
    @SerializedName("ingreds") val ingreds: List<IngredientResponse>
)

data class MyIngredList(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: IngredientListResponse,
    @SerializedName("message") val message: String,
)
