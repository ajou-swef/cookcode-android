package com.swef.cookcode.navifrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.swef.cookcode.R
import com.swef.cookcode.adapter.CookieViewpagerAdapter
import com.swef.cookcode.data.CookieData
import com.swef.cookcode.data.GlobalVariables.cookieAPI
import com.swef.cookcode.data.response.CookieContent
import com.swef.cookcode.data.response.CookieResponse
import com.swef.cookcode.databinding.FragmentCookieBinding
import com.swef.cookcode.`interface`.CookieDeleteListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CookieFragment : Fragment(), CookieDeleteListener {

    private var _binding: FragmentCookieBinding? = null
    private val binding get() = _binding!!

    private lateinit var cookieViewpagerAdapter: CookieViewpagerAdapter

    private var page = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCookieBinding.inflate(inflater, container, false)

        cookieViewpagerAdapter = CookieViewpagerAdapter(requireContext(), this)
        binding.viewPager.apply {
            adapter = cookieViewpagerAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
        }

        getRandomCookies()
        initOnScrollListener()

        return binding.root
    }

    private fun getRandomCookies() {
        cookieAPI.getCookies(page).enqueue(object: Callback<CookieResponse>{
            override fun onResponse(
                call: Call<CookieResponse>,
                response: Response<CookieResponse>
            ) {
                if(response.isSuccessful){
                    if(cookieViewpagerAdapter.datas.isEmpty()) {
                        cookieViewpagerAdapter.datas = getCookieDatasFromResponseData(response.body()!!.data) as MutableList<CookieData>
                        cookieViewpagerAdapter.notifyDataSetChanged()
                    }
                    else {
                        val beforeSize = cookieViewpagerAdapter.itemCount
                        cookieViewpagerAdapter.datas.addAll(getCookieDatasFromResponseData(response.body()!!.data))
                        cookieViewpagerAdapter.notifyItemRangeInserted(beforeSize, cookieViewpagerAdapter.itemCount)
                    }
                }
                else {
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                }
            }

            override fun onFailure(call: Call<CookieResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
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
            val likeNumber = data.likeCount
            val isLiked = data.isLiked
            val madeUser = data.madeUser
            val createdAt = data.createdAt
            val commentCount = data.commentCount

            cookieDatas.add(
                CookieData(cookieId, videoUrl, title, description, madeUser, createdAt, isLiked, likeNumber, commentCount)
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
                if (position == cookieViewpagerAdapter.datas.size - 1) {
                    page++
                    getRandomCookies()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun itemDeleted() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_container, CookieFragment())
            .commit()
    }

    override fun itemDeletedAt(position: Int) {
        // do nothing
    }
}