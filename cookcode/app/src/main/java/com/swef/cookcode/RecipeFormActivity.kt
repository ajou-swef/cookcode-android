package com.swef.cookcode

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.StepRecyclerviewAdapter
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.ActivityRecipeFormBinding
import com.swef.cookcode.`interface`.StepOnClickListener


class RecipeFormActivity : AppCompatActivity(), StepOnClickListener {
    private lateinit var binding : ActivityRecipeFormBinding

    private val stepDatas = mutableListOf<StepData>()

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var stepRecyclerviewAdapter: StepRecyclerviewAdapter

    // 스텝 단계를 위한 변수
    private var numberOfStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 사진을 불러오기 위한 권한 요청
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }

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

        // 스텝 recyclerview 초기화
        stepRecyclerviewAdapter = StepRecyclerviewAdapter(this)
        binding.steps.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.steps.adapter = stepRecyclerviewAdapter

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

                    // 스텝 추가 단계 시 단순 추가 및 스텝 넘버링 +1
                    if (result.data?.getStringExtra("type").equals("add")) {
                        stepDatas.add(stepData)
                        numberOfStep = stepNumber + 1
                    }
                    // 스텝 수정 단계 시 해당 step 정보만 수정
                    else if (result.data?.getStringExtra("type").equals("modify")) {
                        stepDatas[stepNumber] = stepData
                    }
                    // 스텝 삭제 시 해당 스텝 삭제
                    else {
                        deleteStep(stepNumber)
                    }

                    stepRecyclerviewAdapter.datas = stepDatas
                    stepRecyclerviewAdapter.notifyItemChanged(stepData.numberOfStep)
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

    override fun stepOnClick(position: Int){
        val stepData = stepDatas[position - 1]
        val intent = Intent(this, RecipeStepModifyActivity::class.java)
        intent.putExtra("step_images", stepData.imageData.toTypedArray())
        intent.putExtra("step_videos", stepData.videoData?.toTypedArray())
        intent.putExtra("step_title", stepData.title)
        intent.putExtra("step_description", stepData.description)
        intent.putExtra("step_number", stepData.numberOfStep)

        activityResultLauncher.launch(intent)
    }

    private fun deleteStep(position: Int){
        // 삭제 해야할 image, video 정보 저장

        // 단계 삭제
        stepDatas.removeAt(position - 1)

        // 삭제한 단계 이후 단계가 존재한다면 단계 넘버링 수정
        if(position - 1 in stepDatas.indices){
            for(i: Int in position - 1..stepDatas.size){
                stepDatas[i].numberOfStep -= 1
                stepRecyclerviewAdapter.notifyItemChanged(i)
            }
        }
    }
}