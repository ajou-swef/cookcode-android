package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.swef.cookcode.databinding.ActivityHomeBinding
import com.swef.cookcode.navifrags.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    // 로그인 시 발행된 token 정보 불러오기
    private lateinit var accessToken : String
    // refreshtoken은 accesstoken을 refresh하기 위해 사용
    // refreshtoken이 유효하지 않을 경우 로그아웃
    private lateinit var refreshToken : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!intent.getStringExtra("accesstoken").isNullOrBlank()) {
            accessToken = intent.getStringExtra("accesstoken")!!
            Log.d("data", accessToken)
        }
        else {
            Log.d("data", "fail at")
        }

        if(!intent.getStringExtra("refreshtoken").isNullOrBlank()){
            refreshToken = intent.getStringExtra("refreshtoken")!!
            Log.d("data", refreshToken)
        }
        else {
            Log.d("data", "fail rt")
        }
    }

    private fun initBottomNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, HomeFragment())
            .commitAllowingStateLoss()

        binding.bnvMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_cookie -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, CookieFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.menu_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.menu_refrigerator -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, RefrigeratorFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener true
                }
            }
        }
    }
}