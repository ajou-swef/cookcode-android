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
import android.util.Log
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
import com.bumptech.glide.Glide
import com.swef.cookcode.adapter.IngredientRecyclerviewAdapter
import com.swef.cookcode.adapter.StepRecyclerviewAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.host.IngredientDataHost
import com.swef.cookcode.data.response.FileResponse
import com.swef.cookcode.data.response.Photos
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeContentResponse
import com.swef.cookcode.data.response.Step
import com.swef.cookcode.data.response.Videos
import com.swef.cookcode.databinding.ActivityRecipeFormBinding
import com.swef.cookcode.databinding.RecipeIngredientSelectDialogBinding
import com.swef.cookcode.`interface`.StepOnClickListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class RecipeFormActivity : AppCompatActivity(), StepOnClickListener {
    private lateinit var binding : ActivityRecipeFormBinding

    private val stepDatas = mutableListOf<StepData>()
    private var mainImage: String? = null
    private val deleteFiles = mutableListOf<String>()

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
    private var thumbnailUploaded = false
    private var stepExist = false

    private val stepOutOfBound = -1
    private val ERR_RECIPE_CODE = -1

    private val API = RecipeAPI.create()

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

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

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!

        val recipeId = intent.getIntExtra("recipe_id", ERR_RECIPE_CODE)

        // 사진을 불러오기 위한 권한 요청
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }

        // 갤러리에서 image를 불러왔을 때 선택한 image로 수행하는 코드
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val imageFile = makeImageMultipartBody(uri!!)
            postMainImage(accessToken, imageFile)
            thumbnailUploaded = true
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

        // 식재료 선택 다이얼로그
        val dialogView = RecipeIngredientSelectDialogBinding.inflate(layoutInflater)
        dialogView.ingredientName.addTextChangedListener(filteringForKeyword())

        val selectDialog = AlertDialog.Builder(this)
            .setView(dialogView.root)
            .create()

        searchIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe_search")

        val spanCount = 3
        dialogView.recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        dialogView.recyclerView.adapter = searchIngredientRecyclerviewAdapter

        searchIngredientRecyclerviewAdapter.datas = IngredientDataHost().showAllIngredientData() as MutableList<MyIngredientData>
        searchIngredientRecyclerviewAdapter.notifyDataSetChanged()

        dialogView.btnCancel.setOnClickListener {
            selectDialog.dismiss()
        }

        dialogView.ingredientName.setOnFocusChangeListener {
            view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, this)
        }

        // 필수 재료 어댑터
        essentialIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe")
        binding.essentialIngredients.adapter = essentialIngredientRecyclerviewAdapter
        binding.essentialIngredients.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 필수 재료 추가 버튼 클릭시 재료 추가
        binding.addEssentialIngredient.setOnClickListener {
            dialogView.btnDone.setOnClickListener {
                insertDataForEssentialIngredient()
                selectDialog.dismiss()
            }

            // 필수재료와 추가재료는 중복되면 안됨
            removeDupDataInAdditionalIngredient()
            selectDialog.show()
        }

        // 추가 재료 어댑터
        additionalIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("recipe")
        binding.additionalIngredients.adapter = additionalIngredientRecyclerviewAdapter
        binding.additionalIngredients.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 추가 재료 추가 버튼 클릭시 재료 추가
        binding.addAdditionalIngredient.setOnClickListener {
            dialogView.btnDone.setOnClickListener {
                insertDataForAdditionalIngredient()
                selectDialog.dismiss()
            }

            removeDupDataInEssentialIngredient()
            selectDialog.show()
        }

        // 스텝 추가 버튼 클릭시 스텝 제작 화면 띄우기
        binding.addStep.setOnClickListener {
            val intent = Intent(this, RecipeStepActivity::class.java)
            intent.putExtra("step_number", numberOfStep)
            intent.putExtra("access_token", accessToken)
            intent.putExtra("refresh_token", refreshToken)
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
                intent.putExtra("main_image", mainImage)
                intent.putExtra("access_token", accessToken)
                intent.putExtra("refresh_token", refreshToken)

                Log.d("data_size", mainImage!!)

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

                if (recipeId != ERR_RECIPE_CODE) {
                    intent.putExtra("recipe_id", recipeId)
                }

                // 미리보기 종료 시 home activity를 제외한 액티비티는 모두 종료
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                getResult.launch(intent)
            }
            else {
                putToastMessage("추가 재료를 제외한 항목을 입력해주세요.")
            }
        }

        if (recipeId != ERR_RECIPE_CODE) {
            // recipeId가 존재할 경우 수정하는 경우이므로 정보를 불러옴
            getRecipeDataFromRecipeID(recipeId, accessToken)
        }
    }

    override fun onStart() {
        super.onStart()
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_CANCELED) {
                if (result.data != null) {
                    val stepDeleteImages = result.data!!.getStringArrayExtra("step_delete_images")
                    val stepDeleteVideos = result.data!!.getStringArrayExtra("step_delete_videos")
                    if (stepDeleteImages != null) {
                        deleteFiles.addAll(stepDeleteImages.toList())
                    }
                    if (stepDeleteVideos != null){
                        deleteFiles.addAll(stepDeleteVideos.toList())
                    }
                }
            }
            else if (result.resultCode == RESULT_OK) {
                if (result.data != null && !result.data?.getStringExtra("type").equals("delete")) {
                    val stepNumber = result.data?.getIntExtra("step_number", stepOutOfBound)!!
                    val stepImages = result.data?.getStringArrayExtra("images")!!.toList()
                    val stepVideos = result.data?.getStringArrayExtra("videos")!!.toList()
                    val stepTitle = result.data?.getStringExtra("title")!!
                    val stepDescription = result.data?.getStringExtra("description")!!

                    val deleteImages = if (result.data?.getStringArrayExtra("delete_images") != null)
                        result.data?.getStringArrayExtra("delete_images")!!.toList()
                    else emptyList<String>()

                    val deleteVideos = if (result.data?.getStringArrayExtra("delete_videos") != null)
                        result.data?.getStringArrayExtra("delete_videos")!!.toList()
                    else emptyList<String>()

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

                    if (deleteImages.isNotEmpty()) deleteFiles.addAll(deleteImages)
                    if (deleteVideos.isNotEmpty()) deleteFiles.addAll(deleteVideos)

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

    private fun getRecipeDataFromRecipeID(recipeId: Int, accessToken: String) {
        API.getRecipe(accessToken, recipeId).enqueue(object : Callback<RecipeContentResponse> {
            override fun onResponse(
                call: Call<RecipeContentResponse>,
                response: Response<RecipeContentResponse>
            ) {
                if (response.body() != null) {
                    val recipeAndStepData = getRecipeDataFromResponseBody(response.body()!!.recipeData)
                    val recipeData = recipeAndStepData.recipeData
                    val stepDatas = recipeAndStepData.stepData

                    initRecipeForm(recipeData, stepDatas)
                }
                else {
                    putToastMessage("데이터를 불러오는데 실패했습니다.")
                    Log.d("data_size", response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<RecipeContentResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getRecipeDataFromResponseBody(data: RecipeContent): RecipeAndStepData {
        val recipeAndStepData: RecipeAndStepData

        val recipeData = RecipeData(data.recipeId, data.title, data.description, data.mainImage, data.likeCount, data.isCookable, data.user)
        val stepDatas = getStepDatasFromRecipeContent(data.steps)

        recipeAndStepData = RecipeAndStepData(recipeData, stepDatas)

        return recipeAndStepData
    }

    private fun getStepDatasFromRecipeContent(datas: List<Step>): MutableList<StepData> {
        val stepDatas = mutableListOf<StepData>()

        for (item in datas) {
            stepDatas.apply {
                val imageUris = getImageDatasFromStep(item.imageUris)
                val videoUris = getVideoDatasFromStep(item.videoUris)
                val title = item.title
                val description = item.description
                val numberOfStep = item.sequence

                add(StepData(imageUris, videoUris, title, description, numberOfStep))
            }
        }

        return stepDatas
    }

    private fun getImageDatasFromStep(datas: List<Photos>): MutableList<String> {
        val imageUris = mutableListOf<String>()

        for (item in datas) {
            imageUris.add(item.imageUri)
        }

        return imageUris
    }

    private fun getVideoDatasFromStep(datas: List<Videos>): MutableList<String> {
        val videoUris = mutableListOf<String>()

        for (item in datas) {
            videoUris.add(item.videoUri)
        }

        return videoUris
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

        intent.putExtra("access_token", accessToken)
        intent.putExtra("refresh_token", refreshToken)

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
        allIngredientValueTyped = true
        for (item in searchIngredientRecyclerviewAdapter.selectedItems) {
            if (item.value == null) {
                allIngredientValueTyped = false
                break
            }
        }
        return titleTyped && descriptionTyped && essentialIngredientSelected && stepExist && allIngredientValueTyped && thumbnailUploaded
    }

    private fun filteringForKeyword(): TextWatcher {
        return object: TextWatcher {
            override fun beforeTextChanged(currentText: CharSequence?, start: Int, editCount: Int, after: Int) {}

            override fun onTextChanged(currentText: CharSequence?, start: Int, before: Int, editCount: Int) {
                searchIngredientRecyclerviewAdapter.filteredDatas = IngredientDataHost().getIngredientFromNameOrType(
                    searchIngredientRecyclerviewAdapter.beforeSearchData, currentText.toString()) as MutableList<MyIngredientData>
                searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(currentText: Editable?) {}
        }
    }

    private fun removeDupDataInEssentialIngredient() {
        searchIngredientRecyclerviewAdapter.beforeSearchData.clear()
        searchIngredientRecyclerviewAdapter.beforeSearchData = IngredientDataHost().removeElement(
            searchIngredientRecyclerviewAdapter.datas, searchIngredientRecyclerviewAdapter.essentialData)
        searchIngredientRecyclerviewAdapter.filteredDatas = searchIngredientRecyclerviewAdapter.beforeSearchData
        searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
    }

    private fun removeDupDataInAdditionalIngredient() {
        searchIngredientRecyclerviewAdapter.beforeSearchData.clear()
        searchIngredientRecyclerviewAdapter.beforeSearchData = IngredientDataHost().removeElement(
            searchIngredientRecyclerviewAdapter.datas, searchIngredientRecyclerviewAdapter.additionalData)
        searchIngredientRecyclerviewAdapter.filteredDatas = searchIngredientRecyclerviewAdapter.beforeSearchData
        searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
    }

    private fun insertDataForEssentialIngredient() {
        essentialIngredientData = IngredientDataHost().removeElement(
            searchIngredientRecyclerviewAdapter.selectedItems, searchIngredientRecyclerviewAdapter.additionalData)

        essentialIngredientRecyclerviewAdapter.filteredDatas = essentialIngredientData
        searchIngredientRecyclerviewAdapter.essentialData = essentialIngredientData
        essentialIngredientRecyclerviewAdapter.notifyDataSetChanged()
    }

    private fun insertDataForAdditionalIngredient() {
        addtionalIngredientData = IngredientDataHost().removeElement(
            searchIngredientRecyclerviewAdapter.selectedItems, searchIngredientRecyclerviewAdapter.essentialData)

        additionalIngredientRecyclerviewAdapter.filteredDatas = addtionalIngredientData
        searchIngredientRecyclerviewAdapter.additionalData = addtionalIngredientData
        additionalIngredientRecyclerviewAdapter.notifyDataSetChanged()
    }

    private fun hideKeyboardFromEditText(view: View, hasFocus: Boolean, context: Context) {
        val hideFlags = 0
        if (!hasFocus) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, hideFlags)
        }
    }

    private fun postMainImage(accessToken: String, imageFile: MultipartBody.Part) {
        API.postImage(accessToken, imageFile).enqueue(object: Callback<FileResponse>{
            override fun onResponse(call: Call<FileResponse>, response: Response<FileResponse>) {
                if(response.isSuccessful){
                    if (mainImage != null) deleteFiles.add(mainImage!!)
                    val data = response.body()!!.fileUrls.listUrl
                    getImageFromUrl(data[0])
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getImageFromUrl(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(binding.uploadImageBox)

        mainImage = imageUrl
        binding.uploadImageBtn.visibility = View.GONE
    }

    private fun makeImageMultipartBody(uri: Uri): MultipartBody.Part {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "image.jpg") // 임시 파일 생성

        val outputStream: OutputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream) // 이미지를 임시 파일로 복사
        inputStream?.close()
        outputStream.close()

        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("stepFiles", file.name, requestBody)
    }

    private fun putToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun initRecipeForm(recipeData: RecipeData, stepDatas: List<StepData>) {
        getImageFromUrl(recipeData.mainImage)
        thumbnailUploaded = true
        binding.editRecipeName.setText(recipeData.title)
        binding.editDescription.setText(recipeData.description)

        // 필수재료, 추가재료 불러오기

        this.stepDatas.addAll(stepDatas)
        numberOfStep = this.stepDatas.size + 1

        stepRecyclerviewAdapter.datas = this.stepDatas
        stepRecyclerviewAdapter.notifyItemRangeInserted(0, this.stepDatas.size)

        stepExist = true
    }
}


// Inconsistency detected 버그 문제 해결을 위한 솔루션 참조
class LinearLayoutManagerWrapper(context: Context, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}