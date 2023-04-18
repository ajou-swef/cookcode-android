package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.swef.cookcode.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    // 패스워드 일치 확인 검사
    private var isValidPwCheck = false
    // 패스워드 유효성 확인 검사
    private var isCorrectPwCheck = false
    // 이메일 인증 검사
    private var isValidEmailCheck = true
    // 닉네임 중복 검사
    private var isDuplicateNicknameCheck = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // EditText 뷰들 포커스 이벤트 초기화
        setFocusChangeListeners()

        // 완료 버튼 클릭 이벤트
        binding.btnDone.setOnClickListener {
            // 항목 검사 완료 조건
            if (isValidPwCheck && isCorrectPwCheck && isValidEmailCheck && isDuplicateNicknameCheck){
                // API를 통해 회원정보를 서버에 보냄

                // 회원가입 완료 토스트 메시지
                Toast.makeText(this@RegisterActivity, R.string.success_register, Toast.LENGTH_SHORT).show()
                // Activity 종료
                finish()
            }
            // 조건을 충족하지 않았을 경우 토스트 메시지를 띄움
            else {
                Toast.makeText(this@RegisterActivity, R.string.err_type, Toast.LENGTH_SHORT).show()

                // 조건을 만족하지 않은 블록의 테두리를 빨간색으로 조정
                if (isValidPwCheck){
                    binding.editPwValid.setBackgroundResource(R.drawable.round_component_fail)
                }
                if (isCorrectPwCheck){
                    binding.editPw.setBackgroundResource(R.drawable.round_component_fail)
                }
                if (isValidEmailCheck){
                    binding.editId.setBackgroundResource(R.drawable.round_component_fail)
                }
                if (isDuplicateNicknameCheck){
                    binding.editNickname.setBackgroundResource(R.drawable.round_component_fail)
                }
            }
        }
    }

    // EditText 뷰들에 Focus 변경 listener 추가해주는 함수
    private fun setFocusChangeListeners() {
        // edittext 클릭 시 포커스된 뷰 이벤트 처리
        binding.editId.setOnFocusChangeListener { view, gainFocus ->
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

        binding.editNickname.setOnFocusChangeListener { view, gainFocus ->
            if (gainFocus) {
                view.setBackgroundResource(R.drawable.round_component_clicked)
            }
            else {
                // API를 통해 닉네임 중복여부 확인
                // 중복이 아닐경우 만족
                isDuplicateNicknameCheck = true
                view.setBackgroundResource(R.drawable.round_component)
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

    // 영어 소문자, 숫자, 특수문자 3가지 모두 1자 이상 들어가야하며 최소 8자 이상
    private fun isPasswordFormat(password: String): Boolean {
        return password.matches("^(?=.*[a-z])(?=.*[0-9])(?=.*[\$@!%*#?&]).{8,16}.\$".toRegex())
    }
}
