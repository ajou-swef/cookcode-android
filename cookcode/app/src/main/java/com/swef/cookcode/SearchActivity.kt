package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 최근 검색어 보여주기
        binding.recentSearchedWordRecyclerview.adapter

        // 돋보기 버튼 클릭 시 검색
        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchResultActivity::class.java)
            intent.putExtra("keyword", binding.editSearchKeyword.text.toString())
            startActivity(intent)
        }
    }
}