package com.swef.cookcode

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.ActivityRecipeFormBinding


class RecipeFormActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRecipeFormBinding

    private val stepDatas = mutableListOf<StepData>()

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    // 스텝 단계를 위한 변수
    private var numberOfStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 레시피 업로드시 이미지 등록을 위한 변수
        lateinit var recipeImage: Uri

        // 갤러리에서 image를 불러왔을 때 선택한 image로 수행하는 코드
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 이미지 추가하는 버튼 안보이게 하기
            recipeImage = uri!!
            binding.uploadImageBtn.visibility = View.INVISIBLE
            binding.uploadImageBox.setImageURI(recipeImage)
        }

        // 뒤로가기 버튼 클릭시 activity 종료
        binding.beforeArrow.setOnClickListener {
            // 서버에 업로드한 이미지, 영상 삭제

            finish()
        }

        // 이미지 업로드 버튼 클릭시 이미지 업로드
        binding.uploadImageBox.setOnClickListener {
            pickImage.launch("image/*")
        }

        // 필수 재료 추가 버튼 클릭시 재료 추가
        binding.addEssentialIngredient.setOnClickListener {

        }

        // 추가 재료 추가 버튼 클릭시 재료 추가
        binding.addAdditionalIngredient.setOnClickListener {

        }

        // 스텝 추가 버튼 클릭시 스텝 제작 화면 띄우기
        binding.addStep.setOnClickListener {
            val intent = Intent(this, RecipeStepActivity::class.java)
            intent.putExtra("step_number", numberOfStep)

            activityResultLauncher.launch(intent)
        }

        // 미리보기 버튼 클릭시 미리보기 화면 띄우기
        binding.preview.setOnClickListener {

        }
    }

    override fun onStart() {
        super.onStart()
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 받은 데이터 처리
                if (result.data != null) {
                    val stepNumber = result.data?.getIntExtra("step_number", 1)!!
                    val stepImages = result.data?.getStringArrayExtra("images")!!.toList()
                    val stepVideos = result.data?.getStringArrayExtra("videos")!!.toList()
                    val stepTitle = result.data?.getStringExtra("title")!!
                    val stepDescription = result.data?.getStringExtra("description")!!

                    val stepData = StepData(
                        stepImages, stepVideos, stepTitle, stepDescription, stepNumber)

                    stepDatas.add(stepData)
                    numberOfStep = stepNumber + 1
                }
            }
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