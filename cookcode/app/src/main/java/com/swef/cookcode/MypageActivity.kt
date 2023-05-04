package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.swef.cookcode.databinding.ActivityMypageBinding

class MypageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMypageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("user_name")
        binding.userName.text = userName

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 내가 만든 레시피 조회
        binding.madeOwnRecipe.setOnClickListener {

        }

        // 로그아웃
        binding.logout.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("확인"
                ) { _, _ ->
                    Toast.makeText(this@MypageActivity, "정상적으로 로그아웃 되었습니다.", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("취소") { _, _ -> }
                .create()
                .show()
        }

        // 계정 삭제
        binding.btnDeleteUser.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("정말 계정을 삭제 하시겠습니까?")
                .setPositiveButton("확인"
                ) { _, _ ->
                    Toast.makeText(this@MypageActivity, "정상적으로 삭제 되었습니다.", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("취소") { _, _ -> }
                .create()
                .show()
        }
    }
}