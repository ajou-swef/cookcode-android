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

        // 회원가입 클릭시
        binding.btnRegister.setOnClickListener {
            // 회원가입 activity 시작
            val nextIntent = Intent(this, RegisterActivity::class.java)
            startActivity(nextIntent)
        }

        // 아이디/비밀번호 찾기 클릭시
        binding.btnFindIdpw.setOnClickListener {
            // 아이디/비밀번호 찾기 activity 시작
        }

        // 로그인 클릭시
        binding.btnLogin.setOnClickListener {
            // API를 통해 서버에 회원정보를 보냄
            val ID = binding.editId.text.toString()
            val PW = binding.editPw.text.toString()
            // 만족시 HomeActivity로 이동
            val nextIntent = Intent(this, HomeActivity::class.java)
            startActivity(nextIntent)
        }
    }
}