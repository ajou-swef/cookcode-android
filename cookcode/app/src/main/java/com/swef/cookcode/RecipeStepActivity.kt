package com.swef.cookcode

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.swef.cookcode.adapter.StepImageRecyclerviewAdapter
import com.swef.cookcode.adapter.StepVideoRecyclerviewAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.StepImageData
import com.swef.cookcode.data.StepVideoData
import com.swef.cookcode.data.response.FileResponse
import com.swef.cookcode.databinding.ActivityRecipeStepBinding
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


class RecipeStepActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRecipeStepBinding

    // Recyclerview에 넘겨줄 data
    private val imageDatas = mutableListOf<StepImageData>()
    private val videoDatas = mutableListOf<StepVideoData>()

    // RecyclerView Adapter 전역 변수
    private lateinit var stepImageRecyclerviewAdapter: StepImageRecyclerviewAdapter
    private lateinit var stepVideoRecyclerviewAdapter: StepVideoRecyclerviewAdapter

    // 정보 입력 완료 테스트를 위한 변수
    private var titleTyped = false
    private var descriptionTyped = false
    private var imageUploaded = false

    private val API = RecipeAPI.create()

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeStepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!

        // 스텝 단계 넘버링
        val stepNumber = intent.getIntExtra("step_number", -1)
        binding.numberOfStep.text = getString(R.string.step_number, stepNumber)
        binding.addBtn.text = getString(R.string.step_add, stepNumber)

        // recyclerview init
        initImageRecycler()
        initVideoRecycler()

        // 스텝 설명 view init
        initDescriptionTextBox()

        // 뒤로가기 버튼 클릭 시 activity 종료
        binding.beforeArrow.setOnClickListener {
            val intent = Intent()
            if (stepImageRecyclerviewAdapter.deleteImages.isNotEmpty()) {
                intent.putExtra("step_delete_images", stepImageRecyclerviewAdapter.deleteImages.toTypedArray())
                intent.putExtra("step_delete_videos", stepVideoRecyclerviewAdapter.deleteVideos.toTypedArray())
                setResult(RESULT_CANCELED, intent)
            }
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

            // 클릭 된 컴포넌트의 recyclerview를 보여주기
            binding.imageRecyclerview.visibility = View.VISIBLE
            binding.videoRecyclerview.visibility = View.INVISIBLE
        }

        binding.videoBtn.setOnClickListener {
            binding.videoBtn.setBackgroundResource(R.drawable.half_round_component_clicked_right)
            binding.imageBtn.setBackgroundResource(R.drawable.half_round_component)

            binding.imageRecyclerview.visibility = View.INVISIBLE
            binding.videoRecyclerview.visibility = View.VISIBLE
        }

        // 스텝 추가 버튼
        binding.addBtn.setOnClickListener {

            if (testInfoTyped()) {
                // 스텝의 정보들 불러오기
                val imageData = stepImageRecyclerviewAdapter.getData()
                val videoData = stepVideoRecyclerviewAdapter.getData()
                val deleteImageData = stepImageRecyclerviewAdapter.deleteImages.toTypedArray()
                val deleteVideoData = stepVideoRecyclerviewAdapter.deleteVideos.toTypedArray()
                val title = binding.editTitle.text.toString()
                val description = binding.editDescription.text.toString()

                // recipe form activity로 돌아갈때 intent로 정보 넘겨줌
                val intent = Intent()
                intent.putExtra("images", imageData)
                intent.putExtra("videos", videoData)
                intent.putExtra("title", title)
                intent.putExtra("description", description)
                intent.putExtra("step_number", stepNumber)
                intent.putExtra("type", "add")
                intent.putExtra("delete_images", deleteImageData)
                intent.putExtra("delete_videos", deleteVideoData)

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                putToastMessage(stepNumber.toString() + "단계 스텝 작성 완료")
                setResult(RESULT_OK, intent)
                finish()
            }
            else {
                putToastMessage("이미지 한장, 제목, 설명은 필수입니다.")
            }
        }
    }

    // Edittext가 아닌 화면을 터치했을 경우 포커스 해제 및 키보드 숨기기
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            val hideFlags = 0
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)

                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), hideFlags)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // 스텝 설명 edittext 클릭 시 키보드에 잘림 현상 방지
    private fun initDescriptionTextBox(){
        binding.editDescription.setOnFocusChangeListener { view, hasFocus ->
            val defaultX = 0
            val defaultY = 0
            val hideFlags = 0

            if (hasFocus) {
                // EditText가 가려지는 것을 방지하기 위해 ScrollView를 이동
                binding.layout.postDelayed({
                    binding.layout.scrollTo(defaultX, binding.stepDescription.bottom)
                }, 100)
            } else {
                // 포커스 아웃 시 키보드 숨기기
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, hideFlags)

                // ScrollView 원상태로 돌림
                binding.layout.postDelayed({
                    binding.layout.scrollTo(defaultX, defaultY)
                }, 100)
            }
        }
    }

    // imageRecyclerAdapter 초기화
    private fun initImageRecycler() {
        // Recyclerview에서 컴포넌트가 갤러리에서 이미지를 불러올 수 있도록 하는 launcher 구현
        // adapter에서는 registerForActivityResult를 사용할 수 없음
        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 받아온 uri를 datas에 추가하고, 업데이트
            updateRecyclerImage(uri!!)
        }

        // Recyclerview 초기화
        stepImageRecyclerviewAdapter = StepImageRecyclerviewAdapter(pickImageLauncher, this)
        binding.imageRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.imageRecyclerview.adapter = stepImageRecyclerviewAdapter

        // 초기 데이터는 없음
        imageDatas.apply {
            add(StepImageData(null))
            add(StepImageData(null))
            add(StepImageData(null))
        }

        stepImageRecyclerviewAdapter.datas = imageDatas
        // data가 변경되었다는 것을 알림
        stepImageRecyclerviewAdapter.notifyDataSetChanged()
    }

    // Image 선택 시 recyclerview 업데이트
    private fun updateRecyclerImage(imageUri: Uri) {
        for(index: Int in 0..2) {
            if(imageDatas[index].imageUri == null) {
                // 현재 이미지가 추가되어있지 않은 position에 이미지 추가
                imageDatas.removeAt(index)

                val imageFile = makeImageMultipartBody(imageUri)
                putAndGetImageUrl(accessToken, imageFile, index)
                return
            }
        }
    }

    private fun initVideoRecycler(){
        // 동영상을 불러오는 launcher를 구현하여 adapter에 넘겨줌
        val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri?
            -> val videoFile = makeVideoMultipartBody(uri!!)
            putAndGetVideoUrl(accessToken, videoFile)
        }

        stepVideoRecyclerviewAdapter = StepVideoRecyclerviewAdapter(pickVideoLauncher)
        binding.videoRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.videoRecyclerview.adapter = stepVideoRecyclerviewAdapter

        videoDatas.apply {
            add(StepVideoData(null, null))
            add(StepVideoData(null, null))
        }

        stepVideoRecyclerviewAdapter.datas = videoDatas
        stepVideoRecyclerviewAdapter.notifyDataSetChanged()
    }

    // 동영상을 가져왔을 때 썸네일을 뽑아주는 함수
    private fun getVideoInfoAndThumbnail(videoUrl: String) {
        // Glide를 사용해서 thumbnail을 가져온다.
        Glide.with(this)
            .asBitmap()
            .load(videoUrl)
            .into(object: CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    // 아래 resource가 들어간 뷰가 사라지는 등의 경우의 처리
                }

                override fun onResourceReady(thumbnail: Bitmap, transition: Transition<in Bitmap>?) {
                    // 얻어낸 Bitmap 자원을 resource를 통하여 접근
                    // videodata로 return해준다
                    val video = StepVideoData(thumbnail, videoUrl)

                    for(index: Int in 0..2) {
                        if(videoDatas[index].uri == null) {
                            // 현재 비디오가 추가되어있지 않은 position에 비디오 추가
                            videoDatas.removeAt(index)
                            videoDatas.add(index, video)

                            // recyclerview adapter에 해당 위치 알림
                            stepVideoRecyclerviewAdapter.notifyItemChanged(index)
                            return
                        }
                    }
                }
            })
    }

    private fun putAndGetImageUrl(accessToken: String, imageFile: MultipartBody.Part, index: Int) {
        API.postImage(accessToken, imageFile).enqueue(object: Callback<FileResponse> {
            override fun onResponse(call: Call<FileResponse>, response: Response<FileResponse>) {
                if(response.isSuccessful){
                    val data = response.body()!!.fileUrls.listUrl[0]
                    imageDatas.add(index, StepImageData(data))
                    stepImageRecyclerviewAdapter.notifyItemChanged(index)
                    Log.d("data_size", response.body().toString())
                }
                else {
                    Log.d("data_size", response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                putToastMessage("다시 시도해주세요.")
            }
        })
    }

    private fun putAndGetVideoUrl(accessToken: String, videoFile: MultipartBody.Part) {
        API.postImage(accessToken, videoFile).enqueue(object: Callback<FileResponse> {
            override fun onResponse(call: Call<FileResponse>, response: Response<FileResponse>) {
                if(response.isSuccessful){
                    val data = response.body()!!.fileUrls.listUrl[0]
                    getVideoInfoAndThumbnail(data)
                    Log.d("data_size", response.body().toString())
                }
                else {
                    Log.d("data_size", response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<FileResponse>, t: Throwable) {
                putToastMessage("다시 시도해주세요.")
            }
        })
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

    private fun makeVideoMultipartBody(uri: Uri): MultipartBody.Part {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "image.jpg") // 임시 파일 생성

        val outputStream: OutputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream) // 이미지를 임시 파일로 복사
        inputStream?.close()
        outputStream.close()

        val requestBody = file.asRequestBody("video/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("stepFiles", file.name, requestBody)
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun testInfoTyped(): Boolean {
        titleTyped = !binding.editTitle.text.isNullOrEmpty()
        descriptionTyped = !binding.editDescription.text.isNullOrEmpty()
        imageUploaded = stepImageRecyclerviewAdapter.getData().isNotEmpty()

        return titleTyped && descriptionTyped && imageUploaded
    }
}