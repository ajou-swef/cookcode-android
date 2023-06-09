package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.databinding.ActivityHomeBinding
import com.swef.cookcode.navifrags.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    // 로그인 시 발행된 token 정보 불러오기
    private lateinit var accessToken : String
    // refreshtoken은 accesstoken을 refresh하기 위해 사용
    // refreshtoken이 유효하지 않을 경우 로그아웃
    private lateinit var refreshToken : String

    private val USER_ERR_CODE = -1
    private var userId = USER_ERR_CODE

    private val bundle = Bundle()

    private val API = AccountAPI.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!intent.getStringExtra("access_token").isNullOrBlank()) {
            accessToken = intent.getStringExtra("access_token")!!
        }

        if(!intent.getStringExtra("refresh_token").isNullOrBlank()){
            refreshToken = intent.getStringExtra("refresh_token")!!
        }

        userId = intent.getIntExtra("user_id", USER_ERR_CODE)

        bundle.putString("access_token", accessToken)
        bundle.putString("refresh_token", refreshToken)
        bundle.putInt("user_id", userId)

        // bottom navigation bar 초기화
        initBottomNavigation()
    }

    private fun initBottomNavigation() {
        // 최초 실행되는 fragment는 homefragment(메인페이지)
        val homeFragment = HomeFragment()
        val refrigeratorFragment = RefrigeratorFragment()
        val cookieFragment = CookieFragment()

        homeFragment.arguments = bundle
        refrigeratorFragment.arguments = bundle
        cookieFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, homeFragment)
            .commitAllowingStateLoss()
        // 메뉴 역시 home 버튼이 클릭되어있어야함
        binding.bnvMain.selectedItemId = R.id.menu_home

        // 바텀 네비게이션의 click listener
        binding.bnvMain.setOnItemSelectedListener { item ->
            // 각각의 버튼 클릭시 해당 버튼에 해당하는 fragment를 시작함
            when (item.itemId) {
                R.id.menu_cookie -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, cookieFragment)
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, homeFragment)
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_refrigerator -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, refrigeratorFragment)
                        .commitAllowingStateLoss()
                    true
                }

                else -> false
            }
        }
    }

    private fun putToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}