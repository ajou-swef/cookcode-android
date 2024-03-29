package com.swef.cookcode.searchfrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.data.GlobalVariables.recipeAPI
import com.swef.cookcode.data.SearchedRecipeData
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

    private lateinit var searchKeyword: String

    private var cookable = 0
    private var sort = "createdAt"
    private var createdMonth = 5
    private val pageSize = 5
    private var page = 0

    private var hasNext = false
    private var isScrollingUp = false
    private var isScrollingDown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeBinding.inflate(inflater, container, false)
        searchKeyword = arguments?.getString("keyword")!!

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter(requireContext())

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = recyclerViewAdapter
        }

        binding.btnSort.setOnClickListener {
            showPopupMenuToSetSort()
        }

        binding.btnCookable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cookable = 1
                getNewSearchedRecipeDatas()
            } else {
                cookable = 0
                getNewSearchedRecipeDatas()
            }
        }

        initOnScrollListener()
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

    private fun showPopupMenuToSetSort() {
        val popupMenu = PopupMenu(requireContext(), binding.btnSort)
        popupMenu.menuInflater.inflate(R.menu.sort_popup_menu, popupMenu.menu)

        // 팝업 메뉴 아이템 클릭 리스너
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.createdAt -> {
                    sort = "createdAt"
                    getNewSearchedRecipeDatas()
                    binding.btnSort.text = "최신순 정렬"
                    true
                }
                R.id.popular -> {
                    sort = "popular"
                    getNewSearchedRecipeDatas()
                    binding.btnSort.text = "인기순 정렬"
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun getSearchedRecipeDatas() {
        recipeAPI.getSearchRecipes(searchKeyword, cookable, sort, page, pageSize).enqueue(object : Callback<RecipeResponse>{
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful){
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

    private fun getNewSearchedRecipeDatas() {
        page = 0

        recipeAPI.getSearchRecipes(searchKeyword, cookable, sort, page, pageSize).enqueue(object : Callback<RecipeResponse>{
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                putToastMessage("데이터를 불러오고 있습니다.")
                if (response.isSuccessful){
                    val data = response.body()!!.recipes.content
                    val recipeDatas = getRecipeDatasFromResponseBody(data)
                    hasNext = response.body()!!.recipes.hasNext

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

    private fun getRecipeDatasFromResponseBody(datas: List<RecipeContent>): MutableList<SearchedRecipeData> {
        val recipeDatas = mutableListOf<SearchedRecipeData>()

        for (item in datas) {
            val recipeData = SearchedRecipeData(
                item.recipeId, item.title, item.description,
                item.mainImage, item.likeCount, item.isLiked, item.isCookable,
                item.user, item.createdAt.substring(0, 10), item.isPremium, item.isAccessible)
            recipeDatas.add(recipeData)
        }

        return recipeDatas
    }

    private fun initOnScrollListener() {
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 스크롤 상태가 변경되지 않은 경우 (정지 상태)
                    isScrollingUp = false
                    isScrollingDown = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)
                isScrollingUp = differentY > 0
                isScrollingDown = differentY < 0

                if (isScrollingUp) {
                    if (!recyclerView.canScrollVertically(1) && hasNext) {
                        page++
                        getSearchedRecipeDatas()
                        Log.d("data_size", recyclerViewAdapter.datas.toString())
                    }
                }
                else if (isScrollingDown) {
                    if (!recyclerView.canScrollVertically(-1)) {
                        putToastMessage("데이터를 불러오는 중입니다.")
                        getNewSearchedRecipeDatas()
                    }
                }
            }
        })
    }
}