package com.swef.cookcode.searchfrags

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
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeResponse
import com.swef.cookcode.databinding.FragmentSearchRecipeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRecipeFragment : Fragment() {

    private var _binding: FragmentSearchRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var searchKeyword: String

    private var cookable = 1
    private var sort = "createdAt"
    private var createdMonth = 5
    private val pageSize = 10
    private var page = 0

    private val API = RecipeAPI.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeBinding.inflate(inflater, container, false)
        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!
        searchKeyword = arguments?.getString("keyword")!!

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter(requireContext())
        recyclerViewAdapter.accessToken = accessToken
        recyclerViewAdapter.refreshToken = refreshToken

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = recyclerViewAdapter
        }

        initOnScrollListener(linearLayoutManager)

        getSearchedRecipeDatas()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getSearchedRecipeDatas() {
        API.getSearchRecipes(accessToken, searchKeyword, cookable, page, pageSize).enqueue(object : Callback<RecipeResponse>{
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful){
                    val data = response.body()!!.recipes.content
                    val recipeDatas = getRecipeDatasFromResponseBody(data)

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

    private fun getNewSearchedRecipeDatas() {
        page = 0

        API.getSearchRecipes(accessToken, searchKeyword, cookable, page, pageSize).enqueue(object : Callback<RecipeResponse>{
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful){
                    val data = response.body()!!.recipes.content
                    val recipeDatas = getRecipeDatasFromResponseBody(data)

                    recyclerViewAdapter.datas = recipeDatas
                    recyclerViewAdapter.notifyDataSetChanged()
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

    private fun getRecipeDatasFromResponseBody(datas: List<RecipeContent>): MutableList<RecipeData> {
        val recipeDatas = mutableListOf<RecipeData>()

        for (item in datas) {
            val recipeData = RecipeData(
                item.recipeId, item.title, item.description,
                item.mainImage, item.likeCount, item.isLiked, item.isCookable,
                item.user, item.createdAt.substring(0 until 10), item.ingredients, item.additionalIngredients)
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
                        getSearchedRecipeDatas()
                    }
                }

                if(!recyclerView.canScrollVertically(-1)){
                    putToastMessage("데이터를 불러오는 중입니다.")
                    getNewSearchedRecipeDatas()
                }
            }
        })
    }
}