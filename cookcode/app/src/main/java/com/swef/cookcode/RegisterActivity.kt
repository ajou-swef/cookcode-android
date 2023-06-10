package com.swef.cookcode

import android.content.Context
import android.graphics.Rect
import android.icu.text.DecimalFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.swef.cookcode.data.GlobalVariables.authService
import com.swef.cookcode.data.response.CertificationResponse
import com.swef.cookcode.data.response.DuplicateResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import kotlin.concurrent.timer

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private var isValidPwCheck = false
    private var isCorrectPwCheck = false
    private var isDuplicateNicknameCheck = false
    private var isEmailValid = false

    private val nicknameTextWatcher = object: TextWatcher{
        // 변경되기 전
        override fun beforeTextChanged(currentText: CharSequence?, start: Int, editCount: Int, after: Int) {}
        // 변경되는 중
        override fun onTextChanged(currentText: CharSequence?, start: Int, before: Int, editCount: Int) {
            isDuplicateNicknameCheck = false
            binding.testNickname.visibility = View.INVISIBLE
            binding.dupNickText.visibility = View.INVISIBLE
        }
        // 변경된 후
        override fun afterTextChanged(currentText: Editable?) {}
    }

    private lateinit var certificationNumber: String
    private val timeLimit = 180000 // ms, = 3 min
    private lateinit var timerTask: Timer
    private var pastTime = 0 // ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFocusChangeListeners()
    }

    override fun onResume() {
        super.onResume()
        binding.beforeArrow.setOnClickListener{
            finish()
        }

        binding.dupNickTest.setOnClickListener{
            duplicateNicknameTest()
        }
        binding.dupNickTest.bringToFront()

        binding.editNickname.addTextChangedListener(nicknameTextWatcher)

        binding.btnDone.setOnClickListener {
            if (testAllValidCheck()) {
                val id = binding.editId.text.toString()
                val nickname = binding.editNickname.text.toString()
                val pw = binding.editPw.text.toString()

                val userDataMap = HashMap<String, String>()
                userDataMap["email"] = id
                userDataMap["nickname"] = nickname
                userDataMap["password"] = pw

                postUserDataToServer(userDataMap)
            }
            else {
                putToastMessage("제대로 된 값이 입력되지 않았습니다.")
                changeComponentNotValidToRed()
            }
        }

        binding.emailTest.setOnClickListener {
            binding.time.visibility = View.VISIBLE
            getEmailCertificationNumber(binding.editId.text.toString())
            startTimerForEmailValidCheck()
        }
        binding.emailTest.bringToFront()

        binding.emailTestValid.setOnClickListener {
            checkCertificationNumber(binding.editEmailValid.text.toString())
        }
        binding.emailTestValid.bringToFront()
    }

    private fun checkCertificationNumber(number: String) {
        if (pastTime == timeLimit) {
            pastTime = 0
            binding.time.text = "03:00"
            getEmailCertificationNumber(binding.editId.text.toString())
            startTimerForEmailValidCheck()

            putToastMessage("인증시간이 초과되어 새로운 인증번호를 발송합니다.")
        }
        if (number == certificationNumber) {
            timerTask.cancel()
            binding.emailTest.visibility = View.GONE
            binding.emailTestValid.visibility = View.GONE

            binding.editId.focusable = View.NOT_FOCUSABLE
            binding.editEmailValid.focusable = View.NOT_FOCUSABLE

            binding.editId.setBackgroundResource(R.drawable.filled_round_component)

            isEmailValid = true
            putToastMessage("이메일 인증이 완료되었습니다.")
        }
        else {
            putToastMessage("인증번호를 정확히 입력해주세요.")
        }
    }

    private fun startTimerForEmailValidCheck() {
        timerTask = timer(period = 1000) {
            pastTime += 1000

            val decimalFormat = DecimalFormat("00")

            val min = decimalFormat.format((timeLimit - pastTime) / 60000)
            val sec = decimalFormat.format((timeLimit - pastTime) % 60000 / 1000)

            runOnUiThread {
                binding.time.text = "${min}:${sec}"
            }

            if (pastTime == timeLimit) {
                timerTask.cancel()
            }
        }
    }

    private fun getEmailCertificationNumber(email: String) {
        authService.postEmailValid(email).enqueue(object : Callback<CertificationResponse> {
            override fun onResponse(
                call: Call<CertificationResponse>,
                response: Response<CertificationResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("data_size", response.body()!!.toString())
                    if (response.body()?.status == 200) {
                        certificationNumber = response.body()!!.data
                        putToastMessage("인증번호가 발송 되었습니다.\n이메일을 확인해주세요.")
                    } else {
                        binding.editId.setBackgroundResource(R.drawable.round_component_fail)
                        putToastMessage("이미 존재하는 아이디입니다.")
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<CertificationResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun changeComponentNotValidToRed() {
        if (!isValidPwCheck) {
            binding.editPwValid.setBackgroundResource(R.drawable.round_component_fail)
        }
        if (!isCorrectPwCheck) {
            binding.editPw.setBackgroundResource(R.drawable.round_component_fail)
        }
        if (!isDuplicateNicknameCheck) {
            binding.editNickname.setBackgroundResource(R.drawable.round_component_fail)
        }
    }

    private fun postUserDataToServer(userData: HashMap<String, String>) {
        authService.postUserData(userData).enqueue(object: Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                if (response.isSuccessful) {
                    if (response.body()?.status == 201) {
                        putToastMessage("회원가입이 정상 처리되었습니다.")
                        finish()
                    } else {
                        binding.editId.setBackgroundResource(R.drawable.round_component_fail)
                        putToastMessage("이미 존재하는 아이디입니다.")
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }
            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun duplicateNicknameTest() {
        authService.getDupNickTest(binding.editNickname.text.toString()).enqueue(object: Callback<DuplicateResponse> {
            override fun onResponse(call: Call<DuplicateResponse>, response: Response<DuplicateResponse>) {
                if (response.isSuccessful) {
                    val isUnique = response.body()!!.dupData.isUnique
                    isTypedNicknameUnique(isUnique)
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<DuplicateResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun isTypedNicknameUnique(isUnique: Boolean) {
        if (isUnique) {
            isDuplicateNicknameCheck = true
            binding.testNickname.visibility = View.VISIBLE
            binding.dupNickText.visibility = View.GONE
        } else {
            isDuplicateNicknameCheck = false
            binding.testNickname.visibility = View.GONE
            binding.dupNickText.visibility = View.VISIBLE
        }
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun testAllValidCheck(): Boolean =
        isDuplicateNicknameCheck && isValidPwCheck && isCorrectPwCheck && isEmailValid

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

        binding.editEmailValid.setOnFocusChangeListener { view, gainFocus ->
            if (gainFocus) {
                view.setBackgroundResource(R.drawable.round_component_clicked)
            }
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

}
