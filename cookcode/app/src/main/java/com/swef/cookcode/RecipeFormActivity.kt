package com.swef.cookcode

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.swef.cookcode.databinding.ActivityRecipeFormBinding


class RecipeFormActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRecipeFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한 요청


        // 뒤로가기 버튼 클릭시 activity 종료
        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 이미지 업로드 버튼 클릭시 이미지 업로드
        binding.uploadImageBox.setOnClickListener {
        }

        // 필수 재료 추가 버튼 클릭시 재료 추가
        binding.addEssentialIngredient.setOnClickListener {

        }

        // 추가 재료 추가 버튼 클릭시 재료 추가
        binding.addAdditionalIngredient.setOnClickListener {

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