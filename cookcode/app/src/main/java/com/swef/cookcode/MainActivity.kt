package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.swef.cookcode.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // ViewBinding 사용 : xml에 있는 view를 직접 참조할 수 있게 도와주는 기능
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // R.layout.activity_main 대신 뷰바인딩으로 생성된 루트 뷰를 넘겨준다
        setContentView(binding.root)

        // 회원가입 글자 클릭시
        binding.btnRegister.setOnClickListener {
            // 회원가입 activity 시작
            val nextIntent = Intent(this, RegisterActivity::class.java)
            startActivity(nextIntent)
        }
    }
}