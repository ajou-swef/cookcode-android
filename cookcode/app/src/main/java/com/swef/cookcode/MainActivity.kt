package com.swef.cookcode

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.data.response.TokenResponse
import com.swef.cookcode.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    // ViewBinding 사용 : xml에 있는 view를 직접 참조할 수 있게 도와주는 기능
    private lateinit var binding: ActivityMainBinding
    // AccountAPI
    private val API = AccountAPI.create()

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

        // 아이디/비밀번호 찾기 클릭시
        binding.btnFindIdpw.setOnClickListener {
            // 아이디/비밀번호 찾기 activity 시작
        }

        // 로그인 클릭시
        binding.btnLogin.setOnClickListener {
            // API를 통해 서버에 회원정보를 보냄
            val id = binding.editId.text.toString()
            val pw = binding.editPw.text.toString()
            val homeActivityIntent = Intent(this, HomeActivity::class.java)

            val userDataMap = HashMap<String, String>()
            userDataMap["email"] = id
            userDataMap["password"] = pw

            API.postSignin(userDataMap).enqueue(object: Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if(response.body() != null) {
                        // status 200 = 로그인 성공
                        if (response.body()!!.status == 200) {
                            // 만족시 HomeActivity로 이동
                            // 메인 페이지에서 활동 시 Token이 필요하므로 token 정보를 다음 activity로 넘겨준다
                            val userId = response.body()!!.tokenData.userId
                            val accessToken = response.body()!!.tokenData.accessToken
                            val refreshToken = response.body()!!.tokenData.refreshToken

                            // 데이터는 key, value 쌍으로 넘어간다
                            homeActivityIntent.putExtra("access_token", accessToken)
                            homeActivityIntent.putExtra("refresh_token", refreshToken)
                            homeActivityIntent.putExtra("user_id", userId)
                            homeActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(homeActivityIntent)
                            Toast.makeText(this@MainActivity, "정상적으로 로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // null일 경우 일치하는 데이터가 없음
                    else {
                        Toast.makeText(this@MainActivity, R.string.err_userdata, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, R.string.err_server, Toast.LENGTH_SHORT).show()
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
}