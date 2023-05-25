package com.swef.cookcode.navifrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.swef.cookcode.adapter.CookieViewpagerAdapter
import com.swef.cookcode.api.CookieAPI
import com.swef.cookcode.data.CookieData
import com.swef.cookcode.data.response.CookieContent
import com.swef.cookcode.data.response.CookieResponse
import com.swef.cookcode.data.response.MadeUser
import com.swef.cookcode.databinding.FragmentCookieBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CookieFragment : Fragment() {

    private var _binding: FragmentCookieBinding? = null
    private val binding get() = _binding!!

    private lateinit var cookieViewpagerAdapter: CookieViewpagerAdapter

    private val USER_ERR_CODE = -1

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = USER_ERR_CODE

    private val API = CookieAPI.create()
    private var page = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCookieBinding.inflate(inflater, container, false)

        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!
        userId = arguments?.getInt("user_id")!!

        cookieViewpagerAdapter = CookieViewpagerAdapter()
        binding.viewPager.apply {
            adapter = cookieViewpagerAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
        }

        getRandomCookies()
        initOnScrollListener()

        return binding.root
    }

    private fun getRandomCookies() {
        API.getCookies(accessToken, page).enqueue(object: Callback<CookieResponse>{
            override fun onResponse(
                call: Call<CookieResponse>,
                response: Response<CookieResponse>
            ) {
                if(response.isSuccessful){
                    Log.d("data_size", response.body().toString())

                    if(cookieViewpagerAdapter.datas.isEmpty()) {
                        cookieViewpagerAdapter.datas = getCookieDatasFromResponseData(response.body()!!.data.cookies) as MutableList<CookieData>
                        cookieViewpagerAdapter.notifyDataSetChanged()
                    }
                    else {
                        val beforeSize = cookieViewpagerAdapter.itemCount
                        cookieViewpagerAdapter.datas.addAll(getCookieDatasFromResponseData(response.body()!!.data.cookies))
                        cookieViewpagerAdapter.notifyItemRangeInserted(beforeSize, cookieViewpagerAdapter.itemCount)
                    }

                    cookieViewpagerAdapter.hasNext = response.body()!!.data.hasNext
                }
                else {
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                }
            }

            override fun onFailure(call: Call<CookieResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getCookieDatasFromResponseData(datas: List<CookieContent>): List<CookieData>{
        val cookieDatas = mutableListOf<CookieData>()

        for(data in datas){
            val videoUrl = data.videoUrl
            val title = data.title
            val description = data.description
            val cookieId = data.cookieId

            val tempUser = MadeUser(1, "null", "쿡코드짱")
            val tempCreatedAt = "2023-06-07"

            cookieDatas.add(
                CookieData(cookieId, videoUrl, tempUser, tempCreatedAt, false)
            )
        }

        return cookieDatas
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    private fun initOnScrollListener() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val currentPositionInPage = position % 5
                val currentPage = position / 5

                if (testCanGetNextPageOnData(currentPositionInPage, currentPage)) {
                    page++
                    getRandomCookies()
                }

                if (testIsLastData(position)){
                    putToastMessage("마지막 영상입니다.")
                }
            }
        })
    }

    private fun testCanGetNextPageOnData(currentPositionInPage: Int, currentPage: Int): Boolean {
        if (currentPositionInPage != 4) {
            return false
        }
        else if (currentPage != page){
            return false
        }
        else if (!cookieViewpagerAdapter.hasNext){
            return false
        }
        return true
    }

    private fun testIsLastData(currentPosition: Int): Boolean {
        if (currentPosition == cookieViewpagerAdapter.itemCount - 1 && !cookieViewpagerAdapter.hasNext)
            return true
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}