package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityMypageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageActivity : AppCompatActivity() {
    private val ERR_USER_CODE = -1

    private lateinit var binding: ActivityMypageBinding

    private val API = AccountAPI.create()

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = ERR_USER_CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)

        val userName = intent.getStringExtra("user_name")
        binding.userName.text = userName

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 내가 만든 레시피 조회
        binding.madeOwnRecipe.setOnClickListener {

        }

        // 로그아웃
        binding.logout.setOnClickListener { buildAlertDialog("logout") }

        // 계정 삭제
        binding.btnDeleteUser.setOnClickListener { buildAlertDialog("delete") }
    }

    private fun buildAlertDialog(type: String) {
        val message: String
        val toastMessage: String

        if (type == "logout") {
            message = "정말 로그아웃 하시겠습니까?"
            toastMessage = "정상적으로 로그아웃 되었습니다."
        }
        else {
            message = "정말 계정을 삭제 하시겠습니까?"
            toastMessage = "정상적으로 삭제 되었습니다."
        }

        return AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("확인"
            ) { _, _ ->
                val intent = Intent(this, MainActivity::class.java)

                API.patchAccount(accessToken).enqueue(object: Callback<StatusResponse>{
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if (response.isSuccessful){
                            putToastMessage(toastMessage)
                            startActivity(intent)
                        }
                        else {
                            putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                            Log.d("data_size", response.errorBody()!!.string())
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        putToastMessage("잠시 후 다시 시도해주세요.")
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", t.message.toString())
                    }

                })

            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}