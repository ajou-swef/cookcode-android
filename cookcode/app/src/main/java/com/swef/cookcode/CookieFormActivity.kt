package com.swef.cookcode

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.swef.cookcode.adapter.CookieIndividualVideoRecyclerviewAdapter
import com.swef.cookcode.data.GlobalVariables.cookieAPI
import com.swef.cookcode.data.VideoData
import com.swef.cookcode.data.host.ItemTouchCallback
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityCookieFormBinding
import com.swef.cookcode.`interface`.ItemTouchHelperListener
import com.swef.cookcode.`interface`.VideoOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CookieFormActivity : AppCompatActivity(), VideoOnClickListener, ItemTouchHelperListener {

    private lateinit var binding : ActivityCookieFormBinding

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryStartIntent: Intent
    private val videoUrls = mutableListOf<String>()

    private var merged = false
    private var titleTyped = false
    private var descriptionTyped = false
    private var videoUploadedAtLeastOne = false

    private lateinit var mergedVideoFile: String
    private lateinit var videoListTextFile: File
    private lateinit var mergedThumbnail: File

    private lateinit var cookieIndividualVideoRecyclerviewAdapter: CookieIndividualVideoRecyclerviewAdapter
    private val itemTouchHelper by lazy { ItemTouchHelper(ItemTouchCallback(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCookieFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.beforeArrow.setOnClickListener{
            finish()
        }

        initGalleryLauncher()
        initGalleryStartIntent()
        initEditTextViewToKeyboardHide()

        binding.cookieUpload.setOnClickListener{
            if (testInfoTyped()) {
                val multipartBody = makeCookieFormData()
                postCookieData(multipartBody)
            }
        }

        val cacheDir = this.externalCacheDir
        videoListTextFile = File(cacheDir, "videoList.txt")

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.combinedVideo)
        binding.combinedVideo.setMediaController(mediaController)

        binding.combinedVideo.setOnClickListener{
            if (!merged) {
                clickToMerge()
            }
            binding.combinedVideo.start()
        }
        binding.waitingUploadVideos.setOnClickListener {
            if (!merged) {
                clickToMerge()
            }
            binding.combinedVideo.start()
        }

        cookieIndividualVideoRecyclerviewAdapter = CookieIndividualVideoRecyclerviewAdapter(galleryLauncher, galleryStartIntent, this)
        binding.shortVideos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.shortVideos.adapter = cookieIndividualVideoRecyclerviewAdapter

        itemTouchHelper.attachToRecyclerView(binding.shortVideos)

        cookieIndividualVideoRecyclerviewAdapter.datas.add(VideoData(null, null))
        cookieIndividualVideoRecyclerviewAdapter.notifyItemInserted(0)
    }

    private fun initGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                merged = false
                videoUploadedAtLeastOne = false
                binding.waitingUploadVideos.text = "이곳을 더블 클릭하시면 영상을 확인할 수 있습니다."

                val data = result.data
                if (data != null) {
                    if (data.clipData != null) {
                        // 영상이 여러개 선택된 경우
                        val clipData = data.clipData
                        for (index in 0 until clipData!!.itemCount) {
                            val videoUri = clipData.getItemAt(index).uri
                            val tempVideoFile = saveVideoToFile(videoUri)
                            handleVideo(tempVideoFile)
                        }
                    } else if (data.data != null) {
                        // 영상이 하나만 선택된 경우
                        val videoUri = data.data!!
                        val tempVideoFile = saveVideoToFile(videoUri)
                        handleVideo(tempVideoFile)
                    }
                }
            }
        }
    }

    private fun clickToMerge() {
        if(videoUrls.count() == 1) {
            binding.waitingUploadVideos.visibility = View.GONE
            binding.combinedVideo.visibility = View.VISIBLE
            binding.combinedVideo.setVideoURI(Uri.parse(videoUrls[0]))

            val file = File(videoUrls[0])
            mergedVideoFile = file.absolutePath

            binding.combinedVideo.setOnPreparedListener{ mediaPlayer ->
                mediaPlayer.start()
            }

            getVideoThumbnail(mergedVideoFile)
        } else if(videoUrls.count() > 1) {
            binding.waitingUploadVideos.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                val test = withContext(Dispatchers.IO) {
                    mergeVideos(videoUrls)
                }

                if (test != Config.RETURN_CODE_SUCCESS) {
                    binding.waitingUploadVideos.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.combinedVideo.visibility = View.INVISIBLE
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                } else {
                    val videoUri = Uri.parse(mergedVideoFile)
                    binding.waitingUploadVideos.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.combinedVideo.visibility = View.VISIBLE
                    binding.combinedVideo.setVideoURI(videoUri)

                    binding.combinedVideo.setOnPreparedListener{ mediaPlayer ->
                        mediaPlayer.start()
                    }

                    getVideoThumbnail(mergedVideoFile)
                }
            }
        }

        merged = true
        videoUploadedAtLeastOne = true
    }

    private fun initGalleryStartIntent() {
        galleryStartIntent = Intent(Intent.ACTION_PICK)
        galleryStartIntent.type = "video/*"
        galleryStartIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }

    private fun handleVideo(file: File) {
        videoUrls.add(file.absolutePath)
        getVideoInfoAndThumbnail(file.absolutePath)
    }

    private fun mergeVideos(videoPaths: List<String>): Int {
        mergedVideoFile = this.filesDir.absolutePath + "/merged_video.mp4"

        val command = StringBuilder()

        command.append("-y")
        for (videoPath in videoPaths) {
            command.append(" -i ").append(videoPath)
        }
        command.append(" -filter_complex ").append("\"")
        for (i in videoPaths.indices) {
            command.append("[$i:v]setpts=PTS-STARTPTS,setdar=9/16,scale=720x1080,fps=24,format=yuv420p[v$i];")
            command.append("[$i:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo[a$i];")
        }
        for (i in videoPaths.indices) {
            command.append("[v$i][a$i]")
        }
        command.append("concat=n=").append(videoPaths.size).append(":v=1:a=1[v][a]").append("\"")
        command.append(" -map \"[v]\" -map \"[a]\" ").append("-vsync 0 ").append(mergedVideoFile)

        return FFmpeg.execute(command.toString())
    }

    private fun getVideoThumbnail(videoUrl: String) {
        Glide.with(this)
            .asBitmap()
            .load(videoUrl)
            .into(object: CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(thumbnail: Bitmap, transition: Transition<in Bitmap>?) {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

                    if (storageDir != null) {
                        mergedThumbnail = bitmapConvertFile(thumbnail, storageDir.absolutePath + timeStamp)
                    }

                    Log.d("data_size", mergedThumbnail.absolutePath)
                }
            })
    }

    private fun bitmapConvertFile(bitmap: Bitmap, filePath: String): File
    {
        val file = File(filePath)
        var outputStream: OutputStream? = null

        try {
            file.createNewFile()
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } finally {
            outputStream?.close()
        }

        return file
    }

    private fun putToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveVideoToFile(videoUri: Uri): File {
        val inputStream = contentResolver.openInputStream(videoUri)
        val tempFile = getVideoOutputFile()
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun getVideoOutputFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile("VIDEO_$timeStamp", ".mp4", storageDir)
    }

    override fun onItemDelete(position: Int) {
        val removeData = cookieIndividualVideoRecyclerviewAdapter.datas[position]

        cookieIndividualVideoRecyclerviewAdapter.datas.removeAt(position)
        cookieIndividualVideoRecyclerviewAdapter.notifyItemRemoved(position)
        cookieIndividualVideoRecyclerviewAdapter.notifyItemRangeChanged(position, cookieIndividualVideoRecyclerviewAdapter.itemCount)

        for (videoUrl in videoUrls){
            if(videoUrl == removeData.uri){
                videoUrls.remove(videoUrl)
                break
            }
        }

        merged = false
        videoUploadedAtLeastOne = false

        if (videoUrls.isEmpty()){
            binding.waitingUploadVideos.setText(R.string.annotation_cookie_preview)
        }
    }

    private fun getVideoInfoAndThumbnail(videoUrl: String) {
        Glide.with(this)
            .asBitmap()
            .load(videoUrl)
            .into(object: CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(thumbnail: Bitmap, transition: Transition<in Bitmap>?) {
                    cookieIndividualVideoRecyclerviewAdapter.setData(VideoData(thumbnail, videoUrl))
                }
            })
    }

    private fun initEditTextViewToKeyboardHide(){
        binding.editCookieName.setOnFocusChangeListener {
            view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, this)
        }
        binding.editDescription.setOnFocusChangeListener {
            view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, this)
        }
    }

    private fun hideKeyboardFromEditText(view: View, hasFocus: Boolean, context: Context) {
        val hideFlags = 0
        if (!hasFocus) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, hideFlags)
        }
    }

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

    private fun makeCookieFormData(): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()

        val videoFile = File(mergedVideoFile)
        val videoPart = MultipartBody.Part.createFormData(
            "cookieVideo", videoFile.name, videoFile.asRequestBody("video/mp4".toMediaType())
        )

        val cookieTitle = binding.editCookieName.text
        val titlePart = MultipartBody.Part.createFormData("title", cookieTitle.toString())

        val cookieDescription = binding.editDescription.text
        val descriptionPart =
            MultipartBody.Part.createFormData("desc", cookieDescription.toString())

        val thumbnailPart = MultipartBody.Part.createFormData(
            "thumbnail", mergedThumbnail.name, mergedThumbnail.asRequestBody("image/jpg".toMediaType())
        )

        parts.apply {
            add(videoPart)
            add(titlePart)
            add(descriptionPart)
            add(thumbnailPart)
        }

        return parts
    }

    private fun postCookieData(parts: List<MultipartBody.Part>){
        putToastMessage("업로드를 시작합니다.")
        startHomeActivity()

        cookieAPI.postCookie(parts).enqueue(object: Callback<StatusResponse>{
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                if (response.isSuccessful){
                    putToastMessage("쿠키가 정상적으로 업로드 되었습니다.")

                }
                else {
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
                Log.d("data_size", t.message.toString())
                Log.d("data_size", "call: " + call.request().toString())
            }

        })
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun testInfoTyped(): Boolean{
        titleTyped = !binding.editCookieName.text.isNullOrEmpty()
        descriptionTyped = !binding.editDescription.text.isNullOrEmpty()

        if (!titleTyped || !descriptionTyped) {
            putToastMessage("정보가 입력되지 않았습니다.")
        }
        else if (!videoUploadedAtLeastOne) {
            putToastMessage("먼저 합친 영상을 미리보기 해주세요.")
        }

        return titleTyped && descriptionTyped && videoUploadedAtLeastOne
    }

    override fun onItemMove(from: Int, to: Int) {
        val movedItem = cookieIndividualVideoRecyclerviewAdapter.datas[from]
        cookieIndividualVideoRecyclerviewAdapter.datas.removeAt(from)
        cookieIndividualVideoRecyclerviewAdapter.datas.add(to, movedItem)
        cookieIndividualVideoRecyclerviewAdapter.notifyItemMoved(from, to)

        val movedUrl = videoUrls[from]
        videoUrls.removeAt(from)
        videoUrls.add(to, movedUrl)

        videoUploadedAtLeastOne = false
    }
}