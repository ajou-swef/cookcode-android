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

    private val cookieDatas = mutableListOf<CookieData>()

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

        val tempDatas = mutableListOf<CookieData>()

        tempDatas.apply {
            add(CookieData(
                1, "https://cookcode-swef-s3.s3.ap-northeast-2.amazonaws.com/cookie/2023052416592543964d9b296-6ecf-4c00-a5e0-f018ae84018c"
            , MadeUser(1, "null", "쿡코듲장"), "2023-05-25", false))
            add(CookieData(
                1, "https://cookcode-swef-s3.s3.ap-northeast-2.amazonaws.com/cookie/2023052416592543964d9b296-6ecf-4c00-a5e0-f018ae84018c"
                , MadeUser(1, "null", "빈푸"), "2023-05-25", true))
            add(CookieData(
                1, "https://cookcode-swef-s3.s3.ap-northeast-2.amazonaws.com/cookie/2023052416592543964d9b296-6ecf-4c00-a5e0-f018ae84018c"
                , MadeUser(1, "null", "빈푸"), "2023-05-30", true))
            add(CookieData(
                1, "https://cookcode-swef-s3.s3.ap-northeast-2.amazonaws.com/cookie/2023052416592543964d9b296-6ecf-4c00-a5e0-f018ae84018c"
                , MadeUser(1, "null", "99page"), "2023-05-30", false))
        }

        cookieViewpagerAdapter.datas = tempDatas
        cookieViewpagerAdapter.notifyDataSetChanged()

        return binding.root
    }

    /*
    private fun getRandomCookies() {
        API.getCookies(accessToken, page).enqueue(object: Callback<CookieResponse>{
            override fun onResponse(
                call: Call<CookieResponse>,
                response: Response<CookieResponse>
            ) {
                if(response.isSuccessful){

                }
                else {
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    Log.d("data_size", )
                }
            }

            override fun onFailure(call: Call<CookieResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

     */

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}