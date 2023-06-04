package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.databinding.ActivitySearchResultBinding
import com.swef.cookcode.searchfrags.SearchCookieFragment
import com.swef.cookcode.searchfrags.SearchRecipeFragment
import com.swef.cookcode.searchfrags.SearchUserFragment

class SearchResultActivity : AppCompatActivity() {
    companion object {
        const val ERR_USER_CODE = -1
    }
    private lateinit var binding: ActivitySearchResultBinding

    private lateinit var accessToken : String
    private lateinit var refreshToken : String
    private var userId = ERR_USER_CODE

    private lateinit var searchKeyword : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)

        // 검색어를 상단에 보여줌
        searchKeyword = intent.getStringExtra("keyword")!!
        binding.searchKeyword.text = searchKeyword

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 검색어를 누르면 다시 검색하는 화면으로 되돌아가게 함
        binding.searchKeyword.setOnClickListener {
            finish()
        }

        // 레시피, 쿠키, 사용자 버튼 클릭 리스너 초기화
        initButtonOnclick()
    }

    private fun initButtonOnclick(){
        // 검색어를 bundle에 담아 전달함
        val bundle = Bundle()
        bundle.putString("keyword", searchKeyword)
        bundle.putString("access_token", accessToken)
        bundle.putString("refresh_token", refreshToken)
        bundle.putInt("user_id", userId)

        // 최초 실행 되는 화면은 레시피 검색 화면
        showRecipeFragment(bundle)

        // 각 버튼 클릭 시 해당 클릭된 컴포넌트를 제외한 버튼 들은 회색 처리
        // 각 버튼 클릭 시 해당하는 Fragment 실행
        binding.btnRecipe.setOnClickListener {
            showRecipeFragment(bundle)
        }
        binding.btnCookie.setOnClickListener {
            showCookieFragment(bundle)
        }
        binding.btnUser.setOnClickListener {
            showUserFragment(bundle)
        }
    }

    private fun showRecipeFragment(bundle: Bundle) {
        val searchRecipeFragment = SearchRecipeFragment()
        searchRecipeFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, searchRecipeFragment)
            .commitAllowingStateLoss()
        binding.btnRecipe.setBackgroundResource(R.drawable.filled_round_component_clicked)
        binding.btnCookie.setBackgroundResource(R.drawable.filled_round_component)
        binding.btnUser.setBackgroundResource(R.drawable.filled_round_component)
    }

    private fun showCookieFragment(bundle: Bundle) {
        val searchCookieFragment = SearchCookieFragment()
        searchCookieFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, searchCookieFragment)
            .commitAllowingStateLoss()
        binding.btnCookie.setBackgroundResource(R.drawable.filled_round_component_clicked)
        binding.btnRecipe.setBackgroundResource(R.drawable.filled_round_component)
        binding.btnUser.setBackgroundResource(R.drawable.filled_round_component)
    }

    private fun showUserFragment(bundle: Bundle) {
        val searchUserFragment = SearchUserFragment()
        searchUserFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, searchUserFragment)
            .commitAllowingStateLoss()
        binding.btnUser.setBackgroundResource(R.drawable.filled_round_component_clicked)
        binding.btnCookie.setBackgroundResource(R.drawable.filled_round_component)
        binding.btnRecipe.setBackgroundResource(R.drawable.filled_round_component)
    }
}