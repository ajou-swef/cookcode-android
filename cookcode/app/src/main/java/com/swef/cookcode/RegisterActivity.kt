package com.swef.cookcode

import android.content.Context
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.data.response.DuplicateResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    // AccountAPI
    // private val API = AccountAPI.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EditText 뷰들 포커스 이벤트 초기화
        setFocusChangeListeners()

        // 뒤로가기 버튼 클릭시 activity 종료
        binding.beforeArrow.setOnClickListener{
            finish()
        }

        /*
        // 닉네임 중복확인
        binding.dupNickTest.setOnClickListener{
            // API를 통해 서버에 중복 확인
            API.getDupNickTest(binding.editNickname.text.toString()).enqueue(object: Callback<DuplicateResponse> {
                    override fun onResponse(call: Call<DuplicateResponse>, response: Response<DuplicateResponse>) {
                        // 호출 성공
                        // response는 nullable하므로 추가
                        val isUnique = response.body()?.dupData?.isUnique

                        // response를 정상적으로 받아왔을 경우
                        if (isUnique != null) {
                            if (isUnique) {
                                isDuplicateNicknameCheck = true
                                binding.testNickname.visibility = View.VISIBLE
                                binding.dupNickText.visibility = View.INVISIBLE
                            } else {
                                isDuplicateNicknameCheck = false
                                binding.testNickname.visibility = View.INVISIBLE
                                binding.dupNickText.visibility = View.VISIBLE
                            }
                        }
                        // response가 null일 경우
                        else {
                            Toast.makeText(
                                this@RegisterActivity,
                                R.string.err_server,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<DuplicateResponse>, t: Throwable) {
                        // 호출 실패
                        Toast.makeText(this@RegisterActivity, R.string.err_server, Toast.LENGTH_SHORT).show()
                    }
                })
        }
         */

        // 서버 구축 전 임시 code
        binding.dupNickTest.setOnClickListener{
            isDuplicateNicknameCheck = true
            binding.testNickname.visibility = View.VISIBLE
            binding.dupNickText.visibility = View.INVISIBLE
        }

        // 닉네임 중복확인 후 다른 닉네임으로 변경하고 싶을 때
        // 문자를 입력할 경우 중복확인이 해제되도록 해야함
        binding.editNickname.addTextChangedListener(object: TextWatcher{
            // 변경되기 전
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            // 변경되는 중
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isDuplicateNicknameCheck = false
                binding.testNickname.visibility = View.INVISIBLE
                binding.dupNickText.visibility = View.INVISIBLE
            }
            // 변경된 후
            override fun afterTextChanged(p0: Editable?) {}
        })


        // 완료 버튼 클릭 이벤트
        binding.btnDone.setOnClickListener {
            /*
            // API를 통해 email 중복 테스트 실시
            API.getDupNickTest(binding.editNickname.text.toString()).enqueue(object: Callback<DuplicateResponse> {
                override fun onResponse(call: Call<DuplicateResponse>, response: Response<DuplicateResponse>) {
                    val isUnique = response.body()?.dupData?.isUnique

                    if (isUnique != null) {
                        if (isUnique) {
                            isValidEmailCheck = true
                        } else {
                            // isUnique가 false일 경우 이미 존재하는 아이디
                            // 토스트 메세지를 통해 알려줌
                            isValidEmailCheck = false
                            Toast.makeText(this@RegisterActivity, R.string.duplicated_email, Toast.LENGTH_SHORT).show()
                        }
                    }
                    // response가 null일 경우
                    else {
                        Toast.makeText(this@RegisterActivity, R.string.err_server, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DuplicateResponse>, t: Throwable) {
                    // 호출 실패
                    Toast.makeText(this@RegisterActivity, R.string.err_server, Toast.LENGTH_SHORT).show()
                }
            })
             */

            // 서버 구축 전 임시 code
            isValidEmailCheck = binding.editId.text.toString().isNotEmpty()

            // 만족 시
            if (isValidEmailCheck) {
                // 항목 검사 완료 조건
                if (isValidPwCheck && isCorrectPwCheck && isDuplicateNicknameCheck) {
                    /*
                    // API를 통해 회원정보를 서버에 보냄
                    val id = binding.editId.text.toString()
                    val nickname = binding.editNickname.text.toString()
                    val pw = binding.editPw.text.toString()

                    // POST body에 실어보내기 위해 HashMap 사용
                    // 각 body에 들어갈 key, value 매칭
                    var userDataMap = HashMap<String, String>()
                    userDataMap["email"] = id
                    userDataMap["nickname"] = nickname
                    userDataMap["password"] = pw

                    API.postUserData(userDataMap).enqueue(object: Callback<StatusResponse> {
                        override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                            if (response.body()?.status == 201) {
                                // 회원가입 완료 토스트 메시지
                                Toast.makeText(this@RegisterActivity, R.string.success_register, Toast.LENGTH_SHORT).show()
                                // Activity 종료
                                finish()
                            }
                            // 실패 시
                            else {
                                Toast.makeText(this@RegisterActivity, R.string.err_server, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                            Toast.makeText(this@RegisterActivity, R.string.err_server, Toast.LENGTH_SHORT).show()
                        }
                    })
                     */

                    // 서버 구축 전 임시 code
                    Toast.makeText(this@RegisterActivity, R.string.success_register, Toast.LENGTH_SHORT).show()
                    finish()
                }
                // 조건을 충족하지 않았을 경우 토스트 메시지를 띄움
                else {
                    Toast.makeText(this@RegisterActivity, R.string.err_type, Toast.LENGTH_SHORT).show()

                    // 조건을 만족하지 않은 블록의 테두리를 빨간색으로 조정
                    if (!isValidPwCheck) {
                        binding.editPwValid.setBackgroundResource(R.drawable.round_component_fail)
                    }
                    if (!isCorrectPwCheck) {
                        binding.editPw.setBackgroundResource(R.drawable.round_component_fail)
                    }
                    if (!isValidEmailCheck) {
                        binding.editId.setBackgroundResource(R.drawable.round_component_fail)
                    }
                    if (!isDuplicateNicknameCheck) {
                        binding.editNickname.setBackgroundResource(R.drawable.round_component_fail)
                    }
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
                view.setBackgroundResource(R.drawable.round_component)
            }
        }

        // 비밀번호 유효성 확인
        binding.editPw.setOnFocusChangeListener { view, gainFocus ->
            if (gainFocus) {
                view.setBackgroundResource(R.drawable.round_component_clicked)
            }
            else {
                view.setBackgroundResource(R.drawable.round_component)
                // 비밀번호란의 문자열을 불러옴
                val pwTyped = binding.editPw.text.toString()
                // 비밀번호가 입력되었다면 유효성 검사
                if (pwTyped.isNotEmpty()) {
                    // 유효할 경우 green check를 띄움
                    if (isPasswordFormat(pwTyped)) {
                        isCorrectPwCheck = true
                        binding.testPw.setBackgroundResource(R.drawable.green_check)
                        binding.testPw.visibility = View.VISIBLE
                    }
                    // 유효하지 않을 경우 red cross를 띄움
                    else {
                        isCorrectPwCheck = false
                        binding.testPw.setBackgroundResource(R.drawable.red_cross)
                        binding.testPw.visibility = View.VISIBLE
                    }
                }
            }
        }

        // 비밀번호 일치 확인
        binding.editPwValid.setOnFocusChangeListener { view, gainFocus ->
            if (gainFocus) {
                view.setBackgroundResource(R.drawable.round_component_clicked)
            } else {
                view.setBackgroundResource(R.drawable.round_component)
                // 비밀번호와 비밀번호 확인란의 문자열을 불러옴
                val pwTyped = binding.editPw.text.toString()
                val pwValidCheck = binding.editPwValid.text.toString()

                if (pwTyped.isNotEmpty()) {
                    // 일치할 경우 green check를 띄움
                    if (pwTyped.contentEquals(pwValidCheck)) {
                        isValidPwCheck = true
                        binding.testPwValid.setBackgroundResource(R.drawable.green_check)
                        binding.testPwValid.visibility = View.VISIBLE
                    }
                    // 불일치할 경우 red cross를 띄움
                    else {
                        isValidPwCheck = false
                        binding.testPwValid.setBackgroundResource(R.drawable.red_cross)
                        binding.testPwValid.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // 비밀번호 유효성 검사 함수
    // 영어 소문자, 숫자, 특수문자 3가지 모두 1자 이상 들어가야하며 최소 8자 이상
    private fun isPasswordFormat(password: String): Boolean {
        return password.matches("^(?=.*[a-z])(?=.*[0-9])(?=.*[\$@!%*#?&]).{8,}.\$".toRegex())
    }

    // Edittext가 아닌 화면을 터치했을 경우 포커스 해제 및 키보드 숨기기
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // 화면이 터치 되었을 때
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 현재 focus된 view가 Edittext일 경우
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                // view의 절대 좌표를 구한다
                v.getGlobalVisibleRect(outRect)

                // 절대 좌표 이외의 좌표를 클릭했을 경우 포커스 해제 및 키보드 숨기기
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}
