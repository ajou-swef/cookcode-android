package com.swef.cookcode.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.R
import com.swef.cookcode.UserPageActivity
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.host.IngredientDataHost
import com.swef.cookcode.data.response.Ingredient
import com.swef.cookcode.data.response.MadeUser
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.RecipePreviewItemBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipePreviewAdapter(
    recipeData: RecipeData,
    private val context: Context
) : RecyclerView.Adapter<RecipePreviewAdapter.ViewHolder>() {

    companion object{
        const val spanCount = 3
        const val ERR_USER_CODE = -1
    }

    var data = recipeData
    private lateinit var binding: RecipePreviewItemBinding

    private lateinit var essentialIngredientsRecyclerviewAdapter: IngredientRecyclerviewAdapter
    private lateinit var additionalIngredientsRecyclerviewAdapter: IngredientRecyclerviewAdapter

    lateinit var accessToken: String
    lateinit var refreshToken: String
    lateinit var madeUser: MadeUser
    var userId = ERR_USER_CODE

    private val API = RecipeAPI.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = RecipePreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data)
    }

    inner class ViewHolder(
        private val binding: RecipePreviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecipeData){
            getImageFromUrl(item.mainImage, binding.mainImage)
            binding.recipeName.text = context.getString(
                R.string.string_shadow_convert, item.title)
            binding.madeUser.text = item.madeUser.nickname
            binding.descriptionText.text = item.description
            binding.createdAtTime.text = item.createdAt!!.substring(0 until 10)

            if (madeUser.profileImageUri != null) {
                getImageFromUrl(madeUser.profileImageUri!!, binding.userProfileImage)
            }

            // 필수재료, 추가재료 확인
            essentialIngredientsRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe_preview")
            essentialIngredientsRecyclerviewAdapter.filteredDatas =
                makeIngredientToMyIngredientData(item.ingredients) as MutableList<MyIngredientData>
            binding.essentialIngredients.apply {
                adapter = essentialIngredientsRecyclerviewAdapter
                layoutManager = GridLayoutManager(context, spanCount)
            }

            additionalIngredientsRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe_preview")
            additionalIngredientsRecyclerviewAdapter.filteredDatas =
                makeIngredientToMyIngredientData(item.additionalIngredients) as MutableList<MyIngredientData>
            binding.additionalIngredients.apply {
                adapter = additionalIngredientsRecyclerviewAdapter
                layoutManager = GridLayoutManager(context, spanCount)
            }

            if (item.additionalIngredients.isEmpty()) {
                binding.additionalIngredient.visibility = View.GONE
                binding.additionalIngredients.visibility = View.GONE
                binding.divider.visibility = View.GONE
            }

            if (item.isLiked) {
                binding.likeMark.setBackgroundResource(R.drawable.icon_liked)
            }
            else {
                binding.likeMark.setBackgroundResource(R.drawable.icon_unliked)
            }

            binding.likeMark.setOnClickListener {
                putLikeStatus(item)
            }

            binding.madeUser.setOnClickListener {
                startUserPageActivity()
            }
        }
    }

    private fun putLikeStatus(item: RecipeData){
        API.putLikeStatus(accessToken, item.recipeId).enqueue(object : Callback<StatusResponse> {
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {
                if (response.isSuccessful){
                    if(item.isLiked) {
                        binding.likeMark.setBackgroundResource(R.drawable.icon_unliked)
                        item.isLiked = false
                        item.likes--

                    }
                    else {
                        binding.likeMark.setBackgroundResource(R.drawable.icon_liked)
                        item.isLiked = true
                        item.likes++
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        view.clipToOutline = true

        Glide.with(context)
            .load(imageUrl)
            .into(view)
    }

    private fun makeIngredientToMyIngredientData(ingredients: List<Ingredient>): List<MyIngredientData> {
        val myIngredientDatas = mutableListOf<MyIngredientData>()
        for(item in ingredients) {
            val ingredientData = IngredientDataHost().getIngredientFromId(item.ingredId)
            if (ingredientData != null) {
                ingredientData.value = 100
            }
            myIngredientDatas.add(ingredientData!!)
        }

        return myIngredientDatas
    }

    private fun startUserPageActivity() {
        val nextIntent = Intent(context, UserPageActivity::class.java)
        nextIntent.putExtra("access_token", accessToken)
        nextIntent.putExtra("refresh_token", refreshToken)
        nextIntent.putExtra("my_user_id", userId)
        nextIntent.putExtra("user_id", madeUser.userId)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(nextIntent)
    }

    fun putToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}