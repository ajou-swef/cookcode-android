package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    /** ViewBinding을 쓰기 위한 binding 변수 선언 : lateinit을 통해 추후 초기화 하도록 설정 **/
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
    }
}