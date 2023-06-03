package com.swef.cookcode.userinfofrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.UserPageActivity
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeResponse
import com.swef.cookcode.databinding.FragmentUserRecipeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRecipeFragment : Fragment() {

    private var _binding : FragmentUserRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = UserPageActivity.ERR_USER_CODE

    private var page = 0

    private val API = RecipeAPI.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRecipeBinding.inflate(inflater, container, false)

        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!
        userId = arguments?.getInt("user_id")!!

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter(requireContext())
        recyclerViewAdapter.accessToken = accessToken

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = recyclerViewAdapter
        }

        initOnScrollListener(linearLayoutManager)

        getRecipeDataFromUserId()

        return binding.root
    }

    private fun getRecipeDataFromUserId() {
        API.getUsersRecipes(accessToken, userId, page).enqueue(object : Callback<RecipeResponse>{
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()!!.recipes.content
                    val recipeDatas = getRecipeDatasFromResponseBody(data)

                    if (recyclerViewAdapter.datas.isEmpty()) {
                        recyclerViewAdapter.datas = recipeDatas
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                    else {
                        val beforeSize = recyclerViewAdapter.itemCount
                        recyclerViewAdapter.datas.addAll(recipeDatas)
                        recyclerViewAdapter.notifyItemRangeChanged(beforeSize, recyclerViewAdapter.itemCount)
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }

        })
    }

    private fun getNewRecipeDataFromUserId() {
        recyclerViewAdapter.datas.clear()
        page = 0
        getRecipeDataFromUserId()
    }

    private fun getRecipeDatasFromResponseBody(datas: List<RecipeContent>): MutableList<RecipeData> {
        val recipeDatas = mutableListOf<RecipeData>()

        for (item in datas) {
            val recipeData = RecipeData(
                item.recipeId, item.title, item.description,
                item.mainImage, item.likeCount, item.isLiked, item.isCookable,
                item.user, item.createdAt, item.ingredients, item.additionalIngredients)
            recipeDatas.add(recipeData)
        }

        return recipeDatas
    }

    private fun initOnScrollListener(linearLayoutManager: LinearLayoutManager) {
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)

                if(differentY > 0) {
                    val visibleItemCount = linearLayoutManager.childCount
                    val totalItemCount = linearLayoutManager.itemCount
                    val pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        page++
                        getRecipeDataFromUserId()
                    }
                }

                if(!recyclerView.canScrollVertically(-1)){
                    putToastMessage("데이터를 불러오는 중입니다.")
                    getNewRecipeDataFromUserId()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }
}