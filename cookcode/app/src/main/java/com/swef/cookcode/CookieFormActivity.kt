package com.swef.cookcode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.arthenica.mobileffmpeg.Config
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.swef.cookcode.data.VideoData
import com.swef.cookcode.databinding.ActivityCookieFormBinding
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CookieFormActivity : AppCompatActivity() {

    companion object{
        const val ERR_USER_CODE = -1
        const val ERR_COOKIE_CODE = -1
    }

    private lateinit var binding : ActivityCookieFormBinding

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryStartIntent: Intent

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private val videos = mutableListOf<VideoData>()
    private val videoUrls = mutableListOf<String>()

    private var userId = ERR_USER_CODE

    private lateinit var mergedVideoFile: String
    private lateinit var videoListTextFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCookieFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)

        binding.beforeArrow.setOnClickListener{
            // 서버에 업로드한 동영상 삭제
            finish()
        }

        initGalleryLauncher()
        initGalleryStartIntent()

        binding.cookieUpload.setOnClickListener{
            galleryLauncher.launch(galleryStartIntent)
        }

        val cacheDir = this.externalCacheDir
        videoListTextFile = File(cacheDir, "videoList.txt")

        binding.combinedVideo.setOnClickListener{
            binding.combinedVideo.start()
        }
    }

    private fun initGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
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

                if(videoUrls.count() == 1) {
                    binding.combinedVideo.setVideoURI(Uri.parse(videoUrls[0]))
                    binding.combinedVideo.start()
                } else if(videoUrls.count() > 1) {
                    if (mergeVideos(videoUrls) != Config.RETURN_CODE_SUCCESS) {
                        putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    } else {
                        val videoUri = Uri.parse(mergedVideoFile)
                        binding.combinedVideo.setVideoURI(videoUri)
                        binding.combinedVideo.start()
                    }
                }
            }
        }
    }

    private fun initGalleryStartIntent() {
        galleryStartIntent = Intent(Intent.ACTION_PICK)
        galleryStartIntent.type = "video/*"
        galleryStartIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }

    fun handleVideo(file: File) {
        getVideoInfoAndThumbnail(file.absolutePath)
        videoUrls.add(file.absolutePath)
    }

    private fun getVideoInfoAndThumbnail(videoUrl: String) {
        // Glide를 사용해서 thumbnail을 가져온다.
        Glide.with(this)
            .asBitmap()
            .load(videoUrl)
            .into(object: CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {/* Do nothing */}
                override fun onResourceReady(thumbnail: Bitmap, transition: Transition<in Bitmap>?) {
                    // 얻어낸 Bitmap 자원을 resource를 통하여 접근
                    videos.add(VideoData(thumbnail, videoUrl))
                    videoUrls.add(videoUrl)
                }
            })
    }

    fun mergeVideos(videoPaths: List<String>): Int {
        mergedVideoFile = this.filesDir.absolutePath + "/merged_video.mp4"

        val command = StringBuilder()

        command.append("-y")
        for (videoPath in videoPaths) {
            command.append(" -i ").append(videoPath)
        }
        command.append(" -filter_complex ").append("\"")
        for (i in videoPaths.indices) {
            command.append("[$i:v]setpts=PTS-STARTPTS,scale=720x1080,fps=24,format=yuv420p[v$i];")
            command.append("[$i:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo[a$i];")
        }
        for (i in videoPaths.indices) {
            command.append("[v$i][a$i]")
        }
        command.append("concat=n=").append(videoPaths.size).append(":v=1:a=1[v][a]").append("\"")
        command.append(" -map \"[v]\" -map \"[a]\" ").append("-vsync 0 ").append(mergedVideoFile)

        Log.d("data_size", command.toString())

        return FFmpeg.execute(command.toString())
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
}