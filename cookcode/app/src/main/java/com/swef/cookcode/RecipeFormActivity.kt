package com.swef.cookcode

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.IngredientRecyclerviewAdapter
import com.swef.cookcode.adapter.StepRecyclerviewAdapter
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.host.IngredientDataHost
import com.swef.cookcode.databinding.ActivityRecipeFormBinding
import com.swef.cookcode.databinding.RecipeIngredientSelectDialogBinding
import com.swef.cookcode.`interface`.StepOnClickListener

class RecipeFormActivity : AppCompatActivity(), StepOnClickListener {
    private lateinit var binding : ActivityRecipeFormBinding

    private val stepDatas = mutableListOf<StepData>()

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var stepRecyclerviewAdapter: StepRecyclerviewAdapter

    // 식재료 등록 어댑터
    private lateinit var essentialIngredientRecyclerviewAdapter: IngredientRecyclerviewAdapter
    private lateinit var additionalIngredientRecyclerviewAdapter: IngredientRecyclerviewAdapter
    private lateinit var searchIngredientRecyclerviewAdapter: IngredientRecyclerviewAdapter

    private var essentialIngredientData = mutableListOf<MyIngredientData>()
    private var addtionalIngredientData = mutableListOf<MyIngredientData>()

    // 스텝 단계를 위한 변수
    private var numberOfStep = 1

    // 정보 입력 완료 테스트를 위한 변수
    private var titleTyped = false
    private var descriptionTyped = false
    private var essentialIngredientSelected = false
    private var allIngredientValueTyped = false
    private var stepExist = false

    private val stepOutOfBound = -1

    // 미리보기 단계에서 해당 스텝 수정을 위한 스텝 단계 정보 불러오기
    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val stepNumber = data?.getIntExtra("step_number", stepOutOfBound)

            if (stepNumber != stepOutOfBound) {
                stepOnClick(stepNumber!!)
            }
        }
    }

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

        val addTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(currentText: CharSequence?, start: Int, editCount: Int, after: Int) {}

            override fun onTextChanged(currentText: CharSequence?, start: Int, before: Int, editCount: Int) {
                searchIngredientRecyclerviewAdapter.filteredDatas = IngredientDataHost().getIngredientFromNameOrType(
                    searchIngredientRecyclerviewAdapter.beforeSearchData, currentText.toString()) as MutableList<MyIngredientData>
                searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(currentText: Editable?) {}
        }

        // 식재료 선택 다이얼로그
        val dialogView = RecipeIngredientSelectDialogBinding.inflate(layoutInflater)

        dialogView.ingredientName.addTextChangedListener(addTextChangedListener)

        val selectDialog = AlertDialog.Builder(this)
            .setView(dialogView.root)
            .create()

        val spanCount = 3
        searchIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe_search")
        dialogView.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        dialogView.recyclerView.adapter = searchIngredientRecyclerviewAdapter
        searchIngredientRecyclerviewAdapter.datas = IngredientDataHost().showAllIngredientData() as MutableList<MyIngredientData>
        searchIngredientRecyclerviewAdapter.notifyDataSetChanged()

        dialogView.btnCancel.setOnClickListener {
            selectDialog.dismiss()
        }

        dialogView.root.setOnClickListener { view ->
            val hideFlags = 0
            if (view !is EditText) { // v가 EditText 클래스의 인스턴스가 아닐 경우
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, hideFlags) // 키보드를 숨김
            }
            view.clearFocus()
        }

        // 필수 재료 어댑터
        essentialIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe")
        binding.essentialIngredients.adapter = essentialIngredientRecyclerviewAdapter
        binding.essentialIngredients.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 필수 재료 추가 버튼 클릭시 재료 추가
        binding.addEssentialIngredient.setOnClickListener {
            dialogView.btnDone.setOnClickListener {
                essentialIngredientData = IngredientDataHost().removeElement(
                    searchIngredientRecyclerviewAdapter.selectedItems, searchIngredientRecyclerviewAdapter.additionalData)

                essentialIngredientRecyclerviewAdapter.filteredDatas = essentialIngredientData
                searchIngredientRecyclerviewAdapter.essentialData = essentialIngredientData

                essentialIngredientRecyclerviewAdapter.notifyDataSetChanged()
                selectDialog.dismiss()
            }

            // 필수재료와 추가재료는 중복되면 안됨
            searchIngredientRecyclerviewAdapter.beforeSearchData.clear()
            searchIngredientRecyclerviewAdapter.beforeSearchData = IngredientDataHost().removeElement(
                searchIngredientRecyclerviewAdapter.datas, searchIngredientRecyclerviewAdapter.additionalData)
            searchIngredientRecyclerviewAdapter.filteredDatas = searchIngredientRecyclerviewAdapter.beforeSearchData
            searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
            selectDialog.show()
        }

        // 추가 재료 어댑터
        additionalIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe")
        binding.additionalIngredients.adapter = additionalIngredientRecyclerviewAdapter
        binding.additionalIngredients.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 추가 재료 추가 버튼 클릭시 재료 추가
        binding.addAdditionalIngredient.setOnClickListener {
            dialogView.btnDone.setOnClickListener {
                addtionalIngredientData = IngredientDataHost().removeElement(
                    searchIngredientRecyclerviewAdapter.selectedItems, searchIngredientRecyclerviewAdapter.essentialData)

                additionalIngredientRecyclerviewAdapter.filteredDatas = addtionalIngredientData
                searchIngredientRecyclerviewAdapter.additionalData = addtionalIngredientData

                additionalIngredientRecyclerviewAdapter.notifyDataSetChanged()
                selectDialog.dismiss()
            }

            searchIngredientRecyclerviewAdapter.beforeSearchData.clear()
            searchIngredientRecyclerviewAdapter.beforeSearchData = IngredientDataHost().removeElement(
                searchIngredientRecyclerviewAdapter.datas, searchIngredientRecyclerviewAdapter.essentialData)
            searchIngredientRecyclerviewAdapter.filteredDatas = searchIngredientRecyclerviewAdapter.beforeSearchData
            searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
            selectDialog.show()
        }

        // 스텝 추가 버튼 클릭시 스텝 제작 화면 띄우기
        binding.addStep.setOnClickListener {
            val intent = Intent(this, RecipeStepActivity::class.java)
            intent.putExtra("step_number", numberOfStep)

            activityResultLauncher.launch(intent)
        }

        // 스텝 recyclerview 초기화
        stepRecyclerviewAdapter = StepRecyclerviewAdapter(this)
        // Inconsistency detected error 버그 해결을 위한 wrapper
        val linearLayoutManagerWrapper = LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, false)
        binding.steps.layoutManager = linearLayoutManagerWrapper
        binding.steps.adapter = stepRecyclerviewAdapter

        // 미리보기 버튼 클릭시 미리보기 화면 띄우기
        binding.preview.setOnClickListener {
            // 제목 입력 여부
            titleTyped = !binding.editRecipeName.text.isNullOrEmpty()
            // 설명 입력 여부
            descriptionTyped = !binding.editDescription.text.isNullOrEmpty()
            // 필수재료 등록 여부
            essentialIngredientSelected = essentialIngredientData.isNotEmpty()

            if(testInfoTyped()){
                val intent = Intent(this, RecipePreviewActivity::class.java)
                val steps = numberOfStep - 1

                // 레시피 정보 전달
                intent.putExtra("recipe_title", binding.editRecipeName.text.toString())
                intent.putExtra("recipe_description", binding.editDescription.text.toString())
                intent.putExtra("main_image", recipeImage.toString())

                // 필수재료, 추가재료 정보 전달
                val essentialIngreds = mutableListOf<String>()
                val essentialValues = mutableListOf<String>()
                val additionalIngreds = mutableListOf<String>()
                val additionalValues = mutableListOf<String>()

                essentialIngreds.apply {
                    for (item in searchIngredientRecyclerviewAdapter.essentialData) {
                        val ingredId = item.ingredientData.ingredId
                        add(ingredId.toString())
                    }
                }

                essentialValues.apply {
                    for (item in searchIngredientRecyclerviewAdapter.essentialData) {
                        val ingredValue = item.value
                        add(ingredValue.toString())
                    }
                }

                additionalIngreds.apply {
                    for (item in searchIngredientRecyclerviewAdapter.additionalData) {
                        val ingredId = item.ingredientData.ingredId
                        add(ingredId.toString())
                    }
                }

                additionalValues.apply {
                    for (item in searchIngredientRecyclerviewAdapter.additionalData) {
                        val ingredValue = item.value
                        add(ingredValue.toString())
                    }
                }

                intent.putExtra("essential_ingreds", essentialIngreds.toTypedArray())
                intent.putExtra("essential_values", essentialValues.toTypedArray())
                intent.putExtra("additional_ingreds", additionalIngreds.toTypedArray())
                intent.putExtra("additional_values", additionalValues.toTypedArray())

                // 스텝 개수를 알려줌
                intent.putExtra("index", steps)

                // 스텝 별 정보 전달, tag에 각 번호를 달아서 해당 스텝인 것을 알려줌
                for (index: Int in 0 until steps) {
                    intent.putExtra("images$index", stepDatas[index].imageData.toTypedArray())
                    intent.putExtra("videos$index", stepDatas[index].videoData?.toTypedArray())
                    intent.putExtra("title$index", stepDatas[index].title)
                    intent.putExtra("description$index", stepDatas[index].description)
                    intent.putExtra("step_number$index", stepDatas[index].numberOfStep)
                }

                // 미리보기 종료 시 home activity를 제외한 액티비티는 모두 종료
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                getResult.launch(intent)
            }
            else {
                Toast.makeText(this, "추가 재료를 제외한 항목을 입력해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 받은 데이터 처리
                if (result.data != null && !result.data?.getStringExtra("type").equals("delete")) {
                    val stepNumber = result.data?.getIntExtra("step_number", stepOutOfBound)!!
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
                        stepDatas[stepNumber - 1] = stepData
                    }

                    stepRecyclerviewAdapter.datas = stepDatas
                    stepRecyclerviewAdapter.notifyItemChanged(stepData.numberOfStep)
                }
                // 스텝 삭제 시 해당 스텝 삭제
                else {
                    val stepNumber = result.data?.getIntExtra("step_number", 1)!!
                    deleteStep(stepNumber - 1)

                    stepRecyclerviewAdapter.datas = stepDatas
                    stepRecyclerviewAdapter.notifyItemChanged(stepNumber)
                }

                // 스텝이 하나라도 있으면 업로드 가능
                stepExist = stepDatas.isNotEmpty()
            }
        }
    }

    // Edittext가 아닌 화면을 터치했을 경우 포커스 해제 및 키보드 숨기기
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val hideFlags = 0
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)

                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), hideFlags)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // 스텝 수정 단계 시 클릭한 단계의 정보를 넘겨주는 함수
    override fun stepOnClick(position: Int){
        val stepData = stepDatas[position]
        val intent = Intent(this, RecipeStepModifyActivity::class.java)
        intent.putExtra("step_images", stepData.imageData.toTypedArray())
        intent.putExtra("step_videos", stepData.videoData?.toTypedArray())
        intent.putExtra("step_title", stepData.title)
        intent.putExtra("step_description", stepData.description)
        intent.putExtra("step_number", stepData.numberOfStep)

        activityResultLauncher.launch(intent)
    }

    // 스텝 삭제하는 함수
    private fun deleteStep(position: Int){
        // 삭제 해야할 image, video 정보 저장

        // 단계 삭제
        stepDatas.removeAt(position)

        // 삭제한 단계 이후 단계가 존재한다면 단계 넘버링 수정
        if(stepDatas.indices.last > 0 && position in stepDatas.indices){
            for(index: Int in position..stepDatas.size){
                stepDatas[index].numberOfStep -= 1
                stepRecyclerviewAdapter.notifyItemChanged(index)
            }
        }
        numberOfStep -= 1
    }

    // 필수 정보 입력 여부 판단
    private fun testInfoTyped(): Boolean {
        // 모든 식재료에 양이 입력 되었는지 판단
        for (item in searchIngredientRecyclerviewAdapter.selectedItems) {
            if (item.value == null) {
                allIngredientValueTyped = false
                break
            }
        }
        return titleTyped && descriptionTyped && essentialIngredientSelected && stepExist && allIngredientValueTyped
    }
}


// Inconsistency detected 버그 문제 해결을 위한 솔루션 참조
class LinearLayoutManagerWrapper(context: Context, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}