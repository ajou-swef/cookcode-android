package com.swef.cookcode

import android.content.Context
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.swef.cookcode.data.GlobalVariables.accountAPI
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityPasswordModifyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordModifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordModifyBinding

    private var isPasswordValidFormat = false
    private var isPasswordValidCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFocusChangeListeners()
    }

    override fun onResume() {
        super.onResume()

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        binding.btnModify.setOnClickListener {
            val body = HashMap<String, String>()
            body["password"] = binding.editBeforePw.text.toString()
            body["newPassword"] = binding.editNewPw.text.toString()

            if(testValidCheck()) {
                accountAPI.patchPassword(body).enqueue(object: Callback<StatusResponse>{
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if (response.isSuccessful) {
                            putToastMessage("비밀번호 변경이 완료되었습니다.")
                            finish()
                        }
                        else {
                            Log.d("data_size", call.request().toString())
                            Log.d("data_size", response.errorBody()!!.string())
                            putToastMessage("기존 비밀번호를 확인해주세요.")
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        Log.d("data_size", t.message.toString())
                        Log.d("data_size", call.request().toString())
                        putToastMessage("잠시 후 다시 시도해주세요.")
                    }

                })
            }
            else {
                putToastMessage("정확한 정보를 입력해주세요.")
            }
        }
    }

    private fun setFocusChangeListeners() {
        binding.editNewPw.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                isPasswordValidFormat = isPasswordFormat(binding.editNewPw.text.toString())
                changeForCheckImage(isPasswordValidFormat, binding.testPw)
                isPasswordValidCheck = isValidPasswordCheck(binding.editPwValid.text.toString())
                changeForCheckImage(isPasswordValidCheck, binding.testPwValid)
            }
        }

        binding.editPwValid.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                isPasswordValidCheck = isValidPasswordCheck(binding.editPwValid.text.toString())
                changeForCheckImage(isPasswordValidCheck, binding.testPwValid)
            }
        }
    }

    private fun changeForCheckImage(isValid: Boolean, view: View) {
        if (isValid) {
            view.setBackgroundResource(R.drawable.green_check)
            view.visibility = View.VISIBLE
        }
        else {
            view.setBackgroundResource(R.drawable.red_cross)
            view.visibility = View.VISIBLE
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // 화면이 터치 되었을 때
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 현재 focus된 view가 Edittext일 경우
            val view = currentFocus
            val hideFlags = 0

            if (view is EditText) {
                val outRect = Rect()
                // view의 절대 좌표를 구한다
                view.getGlobalVisibleRect(outRect)

                // 절대 좌표 이외의 좌표를 클릭했을 경우 포커스 해제 및 키보드 숨기기
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), hideFlags)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun testValidCheck(): Boolean = isPasswordValidCheck && isPasswordValidFormat

    private fun isPasswordFormat(password: String): Boolean {
        return password.matches("^(?=.*[a-z])(?=.*[0-9])(?=.*[\$@!%*#?&]).{8,}.\$".toRegex())
    }

    private fun isValidPasswordCheck(password: String): Boolean {
        return isPasswordFormat(password) && password == binding.editNewPw.text.toString()
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}