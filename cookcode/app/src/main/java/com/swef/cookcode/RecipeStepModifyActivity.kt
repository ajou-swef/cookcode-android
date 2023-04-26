package com.swef.cookcode

import android.content.Context
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.swef.cookcode.databinding.ActivityRecipeStepModifyBinding

class RecipeStepModifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeStepModifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeStepModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼 클릭 시 activity 종료
        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 최초 실행 시 이미지 추가 탭이 보여지는 상황
        binding.imageBtn.setBackgroundResource(R.drawable.half_round_component_clicked)
        binding.imageRecyclerview.visibility = View.VISIBLE
        binding.videoRecyclerview.visibility = View.INVISIBLE

        // imageBtn 클릭 시 이미지 추가 탭 보여주기
        binding.imageBtn.setOnClickListener {
            // 클릭 된 컴포넌트 박스로 변경
            binding.imageBtn.setBackgroundResource(R.drawable.half_round_component_clicked)
            binding.videoBtn.setBackgroundResource(R.drawable.half_round_component_right)
        }

        binding.videoBtn.setOnClickListener {
            binding.videoBtn.setBackgroundResource(R.drawable.half_round_component_clicked_right)
            binding.imageBtn.setBackgroundResource(R.drawable.half_round_component)
        }

        // 스텝 삭제 버튼
        binding.deleteBtn.setOnClickListener {

        }

        // 스텝 수정 버튼
        binding.modifyBtn.setOnClickListener {

        }
    }

    // Edittext가 아닌 화면을 터치했을 경우 포커스 해제 및 키보드 숨기기
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)

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