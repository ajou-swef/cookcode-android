package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.swef.cookcode.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    companion object {
        const val ERR_USER_CODE = -1
    }

    private lateinit var accessToken : String
    private lateinit var refreshToken : String
    private var userId = ERR_USER_CODE

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 최근 검색어 보여주기
        binding.recentSearchedWordRecyclerview.adapter

        // 돋보기 버튼 클릭 시 검색
        binding.btnSearch.setOnClickListener {
            // 검색어가 입력되지 않았을 경우
            if(binding.editSearchKeyword.text.toString().isEmpty()) {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this, SearchResultActivity::class.java)
                intent.putExtra("keyword", binding.editSearchKeyword.text.toString())
                intent.putExtra("access_token", accessToken)
                intent.putExtra("refresh_token", refreshToken)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
        }
    }
}