package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.databinding.ActivitySearchResultBinding
import com.swef.cookcode.searchfrags.SearchCookieFragment
import com.swef.cookcode.searchfrags.SearchRecipeFragment
import com.swef.cookcode.searchfrags.SearchUserFragment

class SearchResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultBinding

    // 레시피, 쿠키, 사용자 데이터
    private val recipeDatas = mutableListOf<RecipeData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 검색어를 상단에 보여줌
        val searchKeyword = intent.getStringExtra("keyword")!!
        binding.searchKeyword.text = searchKeyword

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 검색어를 누르면 다시 검색하는 화면으로 되돌아가게 함
        binding.searchKeyword.setOnClickListener {
            finish()
        }

        // 레시피, 쿠키, 사용자 버튼 클릭 리스너 초기화
        initButtonOnclick(searchKeyword)

    }

    private fun initButtonOnclick(keyword: String){
        // 검색어를 bundle에 담아 전달함
        val bundle = Bundle()
        bundle.putString("keyword", keyword)

        // 최초 실행 되는 화면은 레시피 검색 화면
        val searchRecipeFragment = SearchRecipeFragment()
        searchRecipeFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, searchRecipeFragment)
            .commitAllowingStateLoss()
        binding.btnRecipe.setBackgroundResource(R.drawable.filled_round_component_clicked)
        binding.btnCookie.setBackgroundResource(R.drawable.filled_round_component)
        binding.btnUser.setBackgroundResource(R.drawable.filled_round_component)

        // 각 버튼 클릭 시 해당 클릭된 컴포넌트를 제외한 버튼 들은 회색 처리
        // 각 버튼 클릭 시 해당하는 Fragment 실행
        binding.btnRecipe.setOnClickListener {
            binding.btnRecipe.setBackgroundResource(R.drawable.filled_round_component_clicked)
            binding.btnCookie.setBackgroundResource(R.drawable.filled_round_component)
            binding.btnUser.setBackgroundResource(R.drawable.filled_round_component)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, searchRecipeFragment)
                .commitAllowingStateLoss()
        }
        binding.btnCookie.setOnClickListener {
            val searchCookieFragment = SearchCookieFragment()
            searchCookieFragment.arguments = bundle

            binding.btnCookie.setBackgroundResource(R.drawable.filled_round_component_clicked)
            binding.btnRecipe.setBackgroundResource(R.drawable.filled_round_component)
            binding.btnUser.setBackgroundResource(R.drawable.filled_round_component)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, searchCookieFragment)
                .commitAllowingStateLoss()
        }
        binding.btnUser.setOnClickListener {
            val searchUserFragment = SearchUserFragment()
            searchUserFragment.arguments = bundle

            binding.btnUser.setBackgroundResource(R.drawable.filled_round_component_clicked)
            binding.btnCookie.setBackgroundResource(R.drawable.filled_round_component)
            binding.btnRecipe.setBackgroundResource(R.drawable.filled_round_component)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, searchUserFragment)
                .commitAllowingStateLoss()
        }
    }
}