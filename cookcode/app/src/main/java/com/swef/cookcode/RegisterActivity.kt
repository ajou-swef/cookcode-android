package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.swef.cookcode.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    // 패스워드 일치 확인 검사
    private var isValidPwCheck = false
    // 패스워드 유효성 확인 검사
    private var isCorrectPwCheck = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // EditText 뷰들 포커스 이벤트 초기화
        setFocusChangeListeners()

        binding.btnDone.setOnClickListener {

        }
    }

    // EditText 뷰들에 Focus 변경 listener 추가해주는 함수
    private fun setFocusChangeListeners() {
        // 비밀번호를 제외하고 클릭 가능한 뷰들 리스트로 묶기
        val clickableViews: List<View> =
            listOf(
                binding.editId, binding.editNickname
            )

        // edittext 클릭 시 포커스된 뷰 이벤트 처리
        for (item in clickableViews) {
            item.setOnFocusChangeListener { view, gainFocus ->
                // 포커스 되었을 때
                if (gainFocus) {
                    // 메인테마 색의 테두리로 변경
                    view.setBackgroundResource(R.drawable.round_component_clicked)
                }
                // 포커스를 잃었을 때
                else {
                    view.setBackgroundResource(R.drawable.round_component)
                }
            }
        }

        // 비밀번호 유효성 확인
        binding.editPw.setOnFocusChangeListener { view, gainFocus ->
            if (gainFocus) {
                view.setBackgroundResource(R.drawable.round_component_clicked)
            }
            else {
                // 비밀번호란의 문자열을 불러옴
                val pwTyped = binding.editPw.text.toString()

                // 유효할 경우 true
                if(isPasswordFormat(pwTyped)){
                    isCorrectPwCheck = true
                }
                // 유효하지 않을 경우 테두리를 빨간색으로 수정
                else {
                    isCorrectPwCheck = false
                    view.setBackgroundResource(R.drawable.round_component_fail)
                }
            }
        }

        // 비밀번호 일치 확인
        binding.editPwValid.setOnFocusChangeListener { view, gainFocus ->
            if (gainFocus) {
                view.setBackgroundResource(R.drawable.round_component_clicked)
            } else {
                // 비밀번호와 비밀번호 확인란의 문자열을 불러옴
                val pwTyped = binding.editPw.text.toString()
                val pwValidCheck = binding.editPwValid.text.toString()

                // 일치할 경우 true
                if (pwTyped.contentEquals(pwValidCheck)) {
                    isValidPwCheck = true
                }
                // 불일치할 경우 테두리를 빨간색으로 수정
                else {
                    isValidPwCheck = false
                    view.setBackgroundResource(R.drawable.round_component_fail)
                }
            }
        }
    }

    private fun isPasswordFormat(password: String): Boolean {
        return password.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@!%*#?&]).{8,16}.\$".toRegex())
    }
}
