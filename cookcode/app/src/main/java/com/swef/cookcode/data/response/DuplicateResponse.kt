package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class DuplicateResponse(
    @SerializedName("status")val status : Int,
    @SerializedName("data")val dupData : DupData
)

data class DupData(
    @SerializedName("isUnique")val isUnique : Boolean
)
