package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName
import java.util.Date

data class IngredientResponse(
    @SerializedName("fridgeIngredId") val fridgeIngredId: Int,
    @SerializedName("ingredId") val IngredId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("expiredAt") val expiredAt: Date,
    @SerializedName("category") val type: String,
    @SerializedName("quantity") val value: Int,
)

data class IngredientListResponse(
    @SerializedName("ingred") val ingreds: List<IngredientResponse>
)

data class MyIngredList(
    @SerializedName("data") val data: IngredientListResponse
)
