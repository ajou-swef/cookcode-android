package com.swef.cookcode

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.swef.cookcode.data.GlobalVariables.accessToken
import com.swef.cookcode.data.GlobalVariables.accountAPI
import com.swef.cookcode.data.GlobalVariables.authService
import com.swef.cookcode.data.GlobalVariables.authority
import com.swef.cookcode.data.GlobalVariables.refreshToken
import com.swef.cookcode.data.GlobalVariables.userId
import com.swef.cookcode.data.response.TokenResponse
import com.swef.cookcode.data.response.UserResponse
import com.swef.cookcode.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    // ViewBinding 사용 : xml에 있는 view를 직접 참조할 수 있게 도와주는 기능
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // R.layout.activity_main 대신 뷰바인딩으로 생성된 루트 뷰를 넘겨준다
        setContentView(binding.root)

        // 회원가입 클릭시
        binding.btnRegister.setOnClickListener {
            // 회원가입 activity 시작
            val nextIntent = Intent(this, RegisterActivity::class.java)
            startActivity(nextIntent)
        }

        // 비밀번호 찾기 클릭시
        binding.btnFindIdpw.setOnClickListener {
            // 비밀번호 찾기 activity 시작
            val nextIntent = Intent(this, FindPasswordActivity::class.java)
            startActivity(nextIntent)
        }

        // 로그인 클릭시
        binding.btnLogin.setOnClickListener {
            // API를 통해 서버에 회원정보를 보냄
            val id = binding.editId.text.toString()
            val pw = binding.editPw.text.toString()

            val userDataMap = HashMap<String, String>()
            userDataMap["email"] = id
            userDataMap["password"] = pw

            authService.postSignin(userDataMap).enqueue(object: Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if(response.body() != null) {
                        if (response.body()!!.status == 200) {
                            userId = response.body()!!.tokenData.userId
                            accessToken = response.body()!!.tokenData.accessToken
                            refreshToken = response.body()!!.tokenData.refreshToken

                            Log.d("data_size", accessToken)
                            getAuthorityFromUserId()
                        }
                    }
                    else {
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", response.errorBody()!!.string())
                        putToastMessage(getString(R.string.err_userdata))
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", t.message.toString())
                    putToastMessage(getString(R.string.err_server))
                }
            })
        }
    }

    // Edittext가 아닌 화면을 터치했을 경우 포커스 해제 및 키보드 숨기기
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // 화면이 터치 되었을 때
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 현재 focus된 view가 Edittext일 경우
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                // view의 절대 좌표를 구한다
                view.getGlobalVisibleRect(outRect)

                // 절대 좌표 이외의 좌표를 클릭했을 경우 포커스 해제 및 키보드 숨기기
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getAuthorityFromUserId() {
        val intent = Intent(this, HomeActivity::class.java)

        accountAPI.getUserInfo(userId).enqueue(object: Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful){
                    authority = response.body()!!.user.authority
                    when (authority) {
                        "ADMIN" -> {
                            // start admin page
                        }
                        else -> {
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            putToastMessage("정상적으로 로그인 되었습니다.")
                        }
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun putToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}