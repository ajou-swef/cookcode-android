package com.swef.cookcode.searchfrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.adapter.SearchCookieRecyclerviewAdapter
import com.swef.cookcode.api.CookieAPI
import com.swef.cookcode.data.SearchCookieData
import com.swef.cookcode.data.response.CookieContent
import com.swef.cookcode.data.response.CookieContentResponse
import com.swef.cookcode.databinding.FragmentSearchCookieBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchCookieFragment : Fragment() {

    companion object {
        const val ERR_USER_CODE = -1
    }

    private var _binding: FragmentSearchCookieBinding? = null
    private val binding get() = _binding!!

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = ERR_USER_CODE
    private lateinit var searchKeyword: String

    private val pageSize = 10
    private var page = 0
    private var hasNext = false

    private lateinit var recyclerViewAdapter : SearchCookieRecyclerviewAdapter

    private val spanCount = 3

    private val API = CookieAPI.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchCookieBinding.inflate(inflater, container, false)

        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!
        userId = arguments?.getInt("user_id")!!
        searchKeyword = arguments?.getString("keyword")!!

        recyclerViewAdapter = SearchCookieRecyclerviewAdapter(requireContext())

        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = gridLayoutManager
        }

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val itemWidth = screenWidth / 3
        val itemHeight = screenHeight / 3

        recyclerViewAdapter.viewWidth = itemWidth
        recyclerViewAdapter.viewHeight = itemHeight

        getCookieDataFromKeyword()
        initOnScrollListener()

        return binding.root
    }

    private fun getCookieDataFromKeyword() {
        API.getSearchCookies(accessToken, searchKeyword, page).enqueue(object :
            Callback<CookieContentResponse> {
            override fun onResponse(
                call: Call<CookieContentResponse>,
                response: Response<CookieContentResponse>
            ) {
                if (response.isSuccessful) {
                    val cookieDatas = getCookieDatasFromResponseData(response.body()!!.data.content)
                    hasNext = response.body()!!.data.hasNext

                    if (recyclerViewAdapter.datas.isEmpty()) {
                        recyclerViewAdapter.datas = cookieDatas as MutableList<SearchCookieData>
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                    else {
                        val beforeSize = recyclerViewAdapter.itemCount
                        recyclerViewAdapter.datas.addAll(cookieDatas)
                        recyclerViewAdapter.notifyItemRangeChanged(
                            beforeSize,
                            recyclerViewAdapter.itemCount
                        )
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<CookieContentResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getCookieDatasFromResponseData(datas: List<CookieContent>): List<SearchCookieData>{
        val userCookieDatas = mutableListOf<SearchCookieData>()

        for(data in datas){
            val thumbnail = data.thumbnail
            val cookieId = data.cookieId
            val likeCount = data.likeCount

            userCookieDatas.add(SearchCookieData(cookieId, thumbnail, likeCount))
        }

        return userCookieDatas
    }

    private fun getNewCookieDataFromKeyword() {
        page = 0
        recyclerViewAdapter.datas.clear()
        getCookieDataFromKeyword()
    }

    private fun initOnScrollListener() {
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)

                if(!recyclerView.canScrollVertically(1) && hasNext) {
                    page++
                    getCookieDataFromKeyword()
                }
                else if(!recyclerView.canScrollVertically(-1)){
                    putToastMessage("데이터를 불러오는 중입니다.")
                    getNewCookieDataFromKeyword()
                }
            }
        })
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}