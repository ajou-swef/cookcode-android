package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.data.response.UserResponse
import com.swef.cookcode.databinding.ActivityUserPageBinding
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

    private val accountAPI = AccountAPI.create()

    override fun onStart() {
        super.onStart()
        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)
        getAuthorityFromUserId()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

    }

    private fun initContentView() {

    }

    private fun getAuthorityFromUserId() {
        accountAPI.getUserInfo(accessToken, userId).enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val authority = response.body()!!.userData.authority
                    authorityCheck(authority)
                }
                else {
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

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}