package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName
import java.util.Date

data class IngredientResponse(
    @SerializedName("fridgeIngredId") val fridgeIngredId: Int,
    @SerializedName("IngredId") val IngredId: Int,
    @SerializedName("expiredAt") val expiredDate: Date,
    @SerializedName("category") val type: String,
    @SerializedName("quantity") val value: Int,
)
