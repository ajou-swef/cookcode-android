package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class HomeActivity : AppCompatActivity() {

    private val intent = getIntent()

    // 로그인 시 발행된 token 정보 불러오기
    // accesstoken은 짧은 주기로 갱신 되어야 하므로 var로 선언
    private var accessToken = intent.getStringExtra("accesstoken")

    // refreshtoken은 accesstoken을 refresh하기 위해 사용
    // refreshtoken이 유효하지 않을 경우 로그아웃
    private val refreshToken = intent.getStringExtra("refreshtoken")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (accessToken != null) {
            Log.d("data", accessToken!!)
        }
        if (refreshToken != null) {
            Log.d("data", refreshToken)
        }
    }
}