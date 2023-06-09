package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.swef.cookcode.adapter.CookieViewpagerAdapter
import com.swef.cookcode.api.CookieAPI
import com.swef.cookcode.data.CookieData
import com.swef.cookcode.data.GlobalVariables.ERR_CODE
import com.swef.cookcode.data.GlobalVariables.cookieAPI
import com.swef.cookcode.data.response.CookieContent
import com.swef.cookcode.data.response.CookieContentResponse
import com.swef.cookcode.data.response.OneCookieResponse
import com.swef.cookcode.databinding.ActivityCookieBinding
import com.swef.cookcode.`interface`.CookieDeleteListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CookieActivity : AppCompatActivity(), CookieDeleteListener {

    private lateinit var binding : ActivityCookieBinding

    private var madeUserId = ERR_CODE
    private var cookieId = ERR_CODE

    private var hasNext = false
    private var page = 0

    private lateinit var viewpagerAdapter: CookieViewpagerAdapter
    private val datas = mutableListOf<CookieData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCookieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.beforeArrow.setOnClickListener{
            finish()
        }

        binding.beforeArrow.bringToFront()

        cookieId = intent.getIntExtra("cookie_id", ERR_CODE)
        madeUserId = intent.getIntExtra("user_id", ERR_CODE)

        viewpagerAdapter = CookieViewpagerAdapter(this, this)
        binding.viewpager.apply {
            adapter = viewpagerAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
        }

        getSelectedCookie()
        initOnScrollListener()
    }

    private fun getSelectedCookie() {
        cookieAPI.getCookie(cookieId).enqueue(object : Callback<OneCookieResponse> {
            override fun onResponse(
                call: Call<OneCookieResponse>,
                response: Response<OneCookieResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()!!.data
                    val selectedCookieData = getCookieDataFromResponseData(data)
                    datas.add(selectedCookieData)

                    viewpagerAdapter.datas = datas
                    viewpagerAdapter.notifyItemRangeChanged(0, 1)

                    getUsersCookies()
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<OneCookieResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getUsersCookies() {
        cookieAPI.getUserCookies(madeUserId, page).enqueue(object : Callback<CookieContentResponse>{
            override fun onResponse(
                call: Call<CookieContentResponse>,
                response: Response<CookieContentResponse>
            ) {
                if (response.isSuccessful) {
                    val cookieDatas = getCookieDatasFromResponseData(response.body()!!.data.content)
                    hasNext = response.body()!!.data.hasNext

                    val beforeSize = viewpagerAdapter.itemCount
                    viewpagerAdapter.datas.addAll(cookieDatas)
                    viewpagerAdapter.notifyItemRangeChanged(beforeSize, viewpagerAdapter.itemCount)
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

    private fun getCookieDataFromResponseData(data: CookieContent): CookieData {
        val videoUrl = data.videoUrl
        val title = data.title
        val description = data.description
        val cookieId = data.cookieId
        val likeNumber = data.likeCount
        val isLiked = data.isLiked
        val madeUser = data.madeUser
        val createdAt = data.createdAt
        val commentCount = data.commentCount

        return CookieData(
            cookieId,
            videoUrl,
            title,
            description,
            madeUser,
            createdAt,
            isLiked,
            likeNumber,
            commentCount
        )
    }

    private fun getCookieDatasFromResponseData(datas: List<CookieContent>): List<CookieData>{
        val cookieDatas = mutableListOf<CookieData>()

        for(data in datas){
            if (data.cookieId == cookieId)
                continue

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

    private fun initOnScrollListener() {
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == viewpagerAdapter.datas.size - 1 && hasNext) {
                    page++
                    getUsersCookies()
                }
            }
        })
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun itemDeleted() {
        //do nothing
    }

    override fun itemDeletedAt(position: Int) {
        viewpagerAdapter.datas.removeAt(position)
        viewpagerAdapter.notifyItemRemoved(position)
    }
}