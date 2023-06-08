package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.User
import com.swef.cookcode.data.response.UserResponse
import com.swef.cookcode.databinding.ActivityUserPageBinding
import com.swef.cookcode.userinfofrags.UserCookieFragment
import com.swef.cookcode.userinfofrags.UserPremiumContentFragment
import com.swef.cookcode.userinfofrags.UserRecipeFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserPageActivity : AppCompatActivity() {

    companion object {
        const val ERR_USER_CODE = -1
    }

    private lateinit var binding : ActivityUserPageBinding

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = ERR_USER_CODE
    private var myUserId = ERR_USER_CODE

    private val bundle = Bundle()

    private val accountAPI = AccountAPI.create()

    private var subscribed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        myUserId = intent.getIntExtra("my_user_id", ERR_USER_CODE)
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)
        getAuthorityFromUserId()

        bundle.putString("access_token", accessToken)
        bundle.putString("refresh_token", refreshToken)
        bundle.putInt("user_id", userId)

        initContentView()

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        changeButtonSubscribed()

        binding.btnSubscribe.setOnClickListener {
            if (subscribed) {
                postSubscribe()
                subscribed = false
                changeButtonSubscribed()
            }
            else {
                postSubscribe()
                subscribed = true
                changeButtonSubscribed()
            }
        }
    }

    private fun changeButtonSubscribed() {
        if (subscribed) {
            binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component)
            binding.btnSubscribe.text = "구독중"
        }
        else {
            binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component_clicked)
            binding.btnSubscribe.text = "구독하기"
        }
    }

    private fun postSubscribe() {
        accountAPI.postUserSubscribe(accessToken, userId).enqueue(object : Callback<StatusResponse>{
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {
                if (!response.isSuccessful){
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun initContentView() {
        if (myUserId == userId) {
            binding.btnSubscribe.visibility = View.GONE
        }

        val recipeFragment = UserRecipeFragment()
        val cookieFragment = UserCookieFragment()
        val premiumContentFragment = UserPremiumContentFragment()

        recipeFragment.arguments = bundle
        cookieFragment.arguments = bundle
        premiumContentFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, recipeFragment)
            .commitAllowingStateLoss()
        selectContentViewListener("recipe")

        binding.recipe.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, recipeFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("recipe")
        }

        binding.cookie.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, cookieFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("cookie")
        }

        binding.premiumContent.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, premiumContentFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("premium")
        }
    }

    private fun selectContentViewListener(content: String) {
        when(content) {
            "recipe" -> {
                binding.recipe.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.recipe.setBackgroundResource(R.drawable.under_bar_component_clicked)
                binding.cookie.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.cookie.setBackgroundResource(R.drawable.under_bar_component)
                binding.premiumContent.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.premiumContent.setBackgroundResource(R.drawable.under_bar_component)
            }
            "cookie" -> {
                binding.recipe.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.recipe.setBackgroundResource(R.drawable.under_bar_component)
                binding.cookie.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.cookie.setBackgroundResource(R.drawable.under_bar_component_clicked)
                binding.premiumContent.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.premiumContent.setBackgroundResource(R.drawable.under_bar_component)
            }
            "premium" -> {
                binding.recipe.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.recipe.setBackgroundResource(R.drawable.under_bar_component)
                binding.cookie.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.cookie.setBackgroundResource(R.drawable.under_bar_component)
                binding.premiumContent.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.premiumContent.setBackgroundResource(R.drawable.under_bar_component_clicked)
            }
        }
    }

    private fun getAuthorityFromUserId() {
        accountAPI.getUserInfo(accessToken, userId).enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val authority = response.body()!!.user.authority
                    authorityCheck(authority)

                    initUserInfo(response.body()!!.user)
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun authorityCheck(authority: String) {
        if (authority == "USER")
            binding.premiumContent.visibility = View.GONE
    }

    private fun initUserInfo(user: User) {
        binding.nicknameTitle.text = user.nickname
        binding.nickname.text = user.nickname
        binding.subscribedUsers.text = getString(R.string.subscribe_users, user.subscriberCount)

        initSubscribeButton(user.isSubscribed)
        if(user.profileImage != null) {
            getImageFromUrl(user.profileImage, binding.userProfile)
        }
    }

    private fun initSubscribeButton(isSubscribed: Boolean) {
        if (isSubscribed) {
            binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component)
            binding.btnSubscribe.text = "구독중"
        }
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .into(view)
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}