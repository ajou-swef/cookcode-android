package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class SignedMembershipResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val membership: List<SignedMembership>
)

data class MembershipResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val membership: List<Membership>
)

data class SignedMembership(
    @SerializedName("membershipId") val membershipId: Int,
    @SerializedName("grade") val grade: String,
    @SerializedName("price") val price: Int,
    @SerializedName("creater") val creater: MadeUser
)

data class Membership(
    @SerializedName("membershipId") val membershipId: Int,
    @SerializedName("grade") val grade: String,
    @SerializedName("price") val price: Int,
)

