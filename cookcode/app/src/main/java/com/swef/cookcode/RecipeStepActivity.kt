package com.swef.cookcode

import android.content.Context
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.StepImageRecyclerviewAdapter
import com.swef.cookcode.adapter.StepVideoRecyclerviewAdapter
import com.swef.cookcode.data.StepImageData
import com.swef.cookcode.data.StepVideoData
import com.swef.cookcode.databinding.ActivityRecipeStepBinding

class RecipeStepActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRecipeStepBinding

    // Recyclerview에 넘겨줄 data
    private val imageDatas = mutableListOf<StepImageData>()
    private val videoDatas = mutableListOf<StepVideoData>()

    // RecyclerView Adapter 전역 변수
    private lateinit var stepImageRecyclerviewAdapter: StepImageRecyclerviewAdapter
    private lateinit var stepVideoRecyclerviewAdapter: StepVideoRecyclerviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeStepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // recyclerview init
        initImageRecycler()
        initVideoRecycler()

        // 스텝 설명 view init
        initDescriptionTextBox()

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

    // 스텝 설명 edittext 클릭 시 키보드에 잘림 현상 방지
    private fun initDescriptionTextBox(){
        binding.editDescription.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // EditText가 가려지는 것을 방지하기 위해 ScrollView를 이동
                binding.layout.postDelayed({
                    binding.layout.scrollTo(0, binding.stepDescription.bottom)
                }, 100)
            } else {
                // 포커스 아웃 시 키보드 숨기기
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                // ScrollView 원상태로 돌림
                binding.layout.postDelayed({
                    binding.layout.scrollTo(0, 0)
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
        stepImageRecyclerviewAdapter = StepImageRecyclerviewAdapter(pickImageLauncher)
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
        // 동영상을 불러오는 launcher를 구현하여 adapter에 넘겨줌
        val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? -> getVideoInfoAndThumbnail(uri!!)
        }

        stepVideoRecyclerviewAdapter = StepVideoRecyclerviewAdapter(pickVideoLauncher)
        binding.videoRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.videoRecyclerview.adapter = stepVideoRecyclerviewAdapter

        videoDatas.apply {
            add(StepVideoData(null, null, null, null, null, null))
            add(StepVideoData(null, null, null, null, null, null))
        }

        stepVideoRecyclerviewAdapter.datas = videoDatas
        stepVideoRecyclerviewAdapter.notifyDataSetChanged()
    }

    // 동영상을 가져왔을 때 썸네일을 뽑아주는 함수
    private fun getVideoInfoAndThumbnail(uri: Uri) {
        // media meta data retriever를 쓰면 간단하게 영상의 정보를 뽑을 수 있다
        val mediaMetadataRetriever = MediaMetadataRetriever()

        // 영상 길이
        val durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationStr?.toLong() ?: 0

        // 영상 가로, 세로 폭
        val widthStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val width = widthStr?.toInt() ?: 0
        val heightStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val height = heightStr?.toInt() ?: 0

        // 0초 일때 사진을 썸네일로 가져옴
        val bitmap = mediaMetadataRetriever.getFrameAtTime(0)

        // videodata로 return해준다
        val video = StepVideoData(bitmap, uri.toString(), uri, duration, width, height)

        for(i: Int in 0..2) {
            if(videoDatas[i].uri == null) {
                // 현재 이미지가 추가되어있지 않은 position에 이미지 추가
                videoDatas.removeAt(i)
                videoDatas.add(i, video)

                // recyclerview adapter에 해당 위치 알림
                stepVideoRecyclerviewAdapter.notifyItemChanged(i)
                return
            }
        }
    }
}