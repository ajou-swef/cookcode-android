package com.swef.cookcode

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.swef.cookcode.adapter.StepImageRecyclerviewAdapter
import com.swef.cookcode.adapter.StepVideoRecyclerviewAdapter
import com.swef.cookcode.data.StepImageData
import com.swef.cookcode.data.StepVideoData
import com.swef.cookcode.databinding.ActivityRecipeStepModifyBinding

class RecipeStepModifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeStepModifyBinding

    private val imageDatas = mutableListOf<StepImageData>()
    private val videoDatas = mutableListOf<StepVideoData>()

    private lateinit var stepImageRecyclerviewAdapter: StepImageRecyclerviewAdapter
    private lateinit var stepVideoRecyclerviewAdapter: StepVideoRecyclerviewAdapter

    private var titleTyped = false
    private var descriptionTyped = false
    private var imageUploaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeStepModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 스텝 단계 넘버링
        val stepNumber = intent.getIntExtra("step_number", 1)
        binding.numberOfStep.text = stepNumber.toString() + "단계"
        binding.modifyBtn.text = stepNumber.toString() + "단계 스텝 수정하기"

        initImageRecycler()
        initVideoRecycler()
        initDescriptionTextBox()

        // 수정 단계는 데이터가 저장되어 있으므로 따로 초기화 하는 함수를 제작
        initModifyStepAcitivity()

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        binding.imageBtn.setBackgroundResource(R.drawable.half_round_component_clicked)
        binding.imageRecyclerview.visibility = View.VISIBLE
        binding.videoRecyclerview.visibility = View.INVISIBLE

        binding.imageBtn.setOnClickListener {
            binding.imageBtn.setBackgroundResource(R.drawable.half_round_component_clicked)
            binding.videoBtn.setBackgroundResource(R.drawable.half_round_component_right)

            binding.imageRecyclerview.visibility = View.VISIBLE
            binding.videoRecyclerview.visibility = View.INVISIBLE
        }

        binding.videoBtn.setOnClickListener {
            binding.videoBtn.setBackgroundResource(R.drawable.half_round_component_clicked_right)
            binding.imageBtn.setBackgroundResource(R.drawable.half_round_component)

            binding.imageRecyclerview.visibility = View.INVISIBLE
            binding.videoRecyclerview.visibility = View.VISIBLE
        }

        // 스텝 삭제 버튼
        binding.deleteBtn.setOnClickListener {
            intent = Intent()
            intent.putExtra("type", "delete")

            Toast.makeText(this, stepNumber.toString() + "단계 스텝 삭제 완료", Toast.LENGTH_SHORT)
                .show()
            setResult(RESULT_OK, intent)
            finish()
        }

        // 스텝 수정 버튼
        binding.modifyBtn.setOnClickListener {
            if (testInfoTyped()) {
                val imageData = stepImageRecyclerviewAdapter.getData()
                val videoData = stepVideoRecyclerviewAdapter.getData()
                val title = binding.editTitle.text.toString()
                val description = binding.editDescription.text.toString()

                val intent = Intent()
                intent.putExtra("images", imageData)
                intent.putExtra("videos", videoData)
                intent.putExtra("title", title)
                intent.putExtra("description", description)
                intent.putExtra("step_number", stepNumber)
                intent.putExtra("type", "modify")

                Toast.makeText(this, stepNumber.toString() + "단계 스텝 수정 완료", Toast.LENGTH_SHORT)
                    .show()
                setResult(RESULT_OK, intent)
                finish()
            }
            else {
                Toast.makeText(this, "이미지 한장, 제목, 설명은 필수입니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun initDescriptionTextBox(){
        binding.editDescription.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.layout.postDelayed({
                    binding.layout.scrollTo(0, binding.stepDescription.bottom)
                }, 100)
            } else {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                binding.layout.postDelayed({
                    binding.layout.scrollTo(0, 0)
                }, 100)
            }
        }
    }

    private fun initImageRecycler() {
        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            updateRecyclerImage(uri!!)
        }

        stepImageRecyclerviewAdapter = StepImageRecyclerviewAdapter(pickImageLauncher)
        binding.imageRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.imageRecyclerview.adapter = stepImageRecyclerviewAdapter

        imageDatas.apply {
            add(StepImageData(null))
            add(StepImageData(null))
            add(StepImageData(null))
        }

        stepImageRecyclerviewAdapter.datas = imageDatas
        // data가 변경되었다는 것을 알림
        stepImageRecyclerviewAdapter.notifyDataSetChanged()
    }

    private fun updateRecyclerImage(imageUri: Uri) {
        for(i: Int in 0..2) {
            if(imageDatas[i].imageUri == null) {
                // 현재 이미지가 추가되어있지 않은 position에 이미지 추가
                imageDatas.removeAt(i)
                imageDatas.add(i, StepImageData(imageUri))

                // recyclerview adapter에 해당 위치 알림
                stepImageRecyclerviewAdapter.notifyItemChanged(i)
                return
            }
        }
    }

    private fun initVideoRecycler(){
        val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
                uri: Uri? -> getVideoInfoAndThumbnail(uri!!)
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

    private fun getVideoInfoAndThumbnail(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(uri)
            .into(object: CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(thumbnail: Bitmap, transition: Transition<in Bitmap>?) {
                    val video = StepVideoData(thumbnail, uri)

                    for(i: Int in 0..2) {
                        if(videoDatas[i].uri == null) {
                            videoDatas.removeAt(i)
                            videoDatas.add(i, video)

                            // recyclerview adapter에 해당 위치 알림
                            stepVideoRecyclerviewAdapter.notifyItemChanged(i)
                            return
                        }
                    }
                }
            })
    }

    private fun testInfoTyped(): Boolean {
        titleTyped = !binding.editTitle.text.isNullOrEmpty()
        descriptionTyped = !binding.editDescription.text.isNullOrEmpty()
        imageUploaded = stepImageRecyclerviewAdapter.getData().isNotEmpty()

        return titleTyped && descriptionTyped && imageUploaded
    }

    private fun initModifyStepAcitivity() {
        // 저장된 스텝 정보 데이터 넘겨받기
        val stepImages = intent.getStringArrayExtra("step_images")!!.toList()
        val stepVideos = intent.getStringArrayExtra("step_videos")?.toList()
        val stepTitle = intent.getStringExtra("step_title")
        val stepDescription = intent.getStringExtra("step_description")

        // 스텝 정보 불러오기
        binding.editTitle.setText(stepTitle)
        binding.editDescription.setText(stepDescription)

        // String으로 cast된 Uri를 다시 Uri로 변환하여 image 불러오기
        for(i: Int in stepImages.indices){
            imageDatas[i] = (StepImageData(Uri.parse(stepImages[i])))
            stepImageRecyclerviewAdapter.notifyItemChanged(i)
        }

        // String으로 cast된 Uri로 변환하여 video 업로드 후 썸네일 추출
        if(stepVideos?.isNotEmpty() == true) {
            for (i: Int in stepVideos.indices){
                getVideoInfoAndThumbnail(stepVideos[i].toUri())
            }
        }
    }
}