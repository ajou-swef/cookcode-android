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
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.data.GlobalVariables.ERR_CODE
import com.swef.cookcode.data.GlobalVariables.recipeAPI
import com.swef.cookcode.data.SearchedRecipeData
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

    private var page = 0
    private val pageSize = 10
    private var hasNext = false

    private var madeUserId = ERR_CODE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRecipeBinding.inflate(inflater, container, false)

        madeUserId = arguments?.getInt("user_id")!!

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter(requireContext())

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = recyclerViewAdapter
        }

        getNewRecipeDataFromUserId()
        initOnScrollListener()

        return binding.root
    }

    private fun getRecipeDataFromUserId() {
        recipeAPI.getUserRecipes(madeUserId, page).enqueue(object : Callback<RecipeResponse>{
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()!!.recipes.content
                    val recipeDatas = getRecipeDatasFromResponseBody(data)
                    hasNext = response.body()!!.recipes.hasNext

                    if (recyclerViewAdapter.datas.isEmpty()) {
                        recyclerViewAdapter.datas = recipeDatas
                        recyclerViewAdapter.notifyItemRangeChanged(0, recipeDatas.size)
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

    private fun getRecipeDatasFromResponseBody(datas: List<RecipeContent>): MutableList<SearchedRecipeData> {
        val recipeDatas = mutableListOf<SearchedRecipeData>()

        for (item in datas) {
            val recipeData = SearchedRecipeData(
                item.recipeId, item.title, item.description,
                item.mainImage, item.likeCount, item.isLiked, item.isCookable,
                item.user, item.createdAt.substring(0 until 10), item.isPremium, item.isAccessible)
            recipeDatas.add(recipeData)
        }

        return recipeDatas
    }

    private fun initOnScrollListener() {
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)

                if (recyclerViewAdapter.datas.isNotEmpty()) {
                    if (!recyclerView.canScrollVertically(1) && hasNext) {
                        page++
                        getRecipeDataFromUserId()
                    } else if (!recyclerView.canScrollVertically(-1)) {
                        putToastMessage("데이터를 불러오는 중입니다.")
                        getNewRecipeDataFromUserId()
                    }
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