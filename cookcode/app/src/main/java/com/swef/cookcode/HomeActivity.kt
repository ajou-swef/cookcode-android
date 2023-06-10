package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.databinding.ActivityHomeBinding
import com.swef.cookcode.navifrags.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // bottom navigation bar 초기화
        initBottomNavigation()
    }

    private fun initBottomNavigation() {
        // 최초 실행되는 fragment는 homefragment(메인페이지)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, HomeFragment())
            .commitAllowingStateLoss()
        // 메뉴 역시 home 버튼이 클릭되어있어야함
        binding.bnvMain.selectedItemId = R.id.menu_home

        // 바텀 네비게이션의 click listener
        binding.bnvMain.setOnItemSelectedListener { item ->
            // 각각의 버튼 클릭시 해당 버튼에 해당하는 fragment를 시작함
            when (item.itemId) {
                R.id.menu_cookie -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, CookieFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, HomeFragment())
                        .commitAllowingStateLoss()
                    true
                }

                R.id.menu_refrigerator -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_container, RefrigeratorFragment())
                        .commitAllowingStateLoss()
                    true
                }

                else -> false
            }
        }
    }
}