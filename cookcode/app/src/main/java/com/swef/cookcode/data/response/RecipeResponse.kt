package com.swef.cookcode.data.response
import com.google.gson.annotations.SerializedName
import java.util.Date

data class RecipeResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val recipes: List<RecipeContent>,
    @SerializedName("numberOfElements") val numberOfElements: Int, // 무한스크롤 시 현재 페이지에 있는 레시피 개수
    @SerializedName("offset") val offset: Int,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Int, // 전체 레시피 개수
    @SerializedName("totalPages") val totalPages: Int
)

data class RecipeContent(
    @SerializedName("recipeId") val recipeId: Int,
    @SerializedName("user") val user: MadeUser,
    @SerializedName("steps") val steps: List<Step>,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("updatedAt") val updatedAt: Date,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("commentCount") val commentCount: Int,
    @SerializedName("isCookable") val isCookable: Boolean,
    @SerializedName("thumbnail") val mainImage: String
)

data class MadeUser(
    @SerializedName("userId") val userId: Int,
    @SerializedName("profileImage") val profileImageUri: String,
    @SerializedName("nickname") val nickname: String
)
data class Step(
    @SerializedName("stepId") val stepId: Int,
    @SerializedName("seq") val sequence: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("photos") val imageUris: List<Photos>,
    @SerializedName("videos") val videoUris: List<Videos>
)

data class Photos(
    @SerializedName("stepPhotoId") val imageId: Int,
    @SerializedName("photoUrl") val imageUri: String
)

data class Videos(
    @SerializedName("stepPhotoId") val videoId: Int,
    @SerializedName("photoUrl") val videoUri: String
)