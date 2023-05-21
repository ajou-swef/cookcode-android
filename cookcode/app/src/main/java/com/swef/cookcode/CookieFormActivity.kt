package com.swef.cookcode

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.swef.cookcode.databinding.ActivityCookieFormBinding

class CookieFormActivity : AppCompatActivity() {

    companion object{
        const val ERR_USER_CODE = -1
        const val ERR_COOKIE_CODE = -1
        const val REQUEST_CODE = 1
    }

    private lateinit var binding : ActivityCookieFormBinding

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryStartIntent: Intent

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private val videos = mutableListOf<Uri>()

    private var userId = ERR_USER_CODE

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
    }

    private fun initGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    if (data.clipData != null) {
                        // 영상이 여러개 선택된 경우
                        val clipData = data.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            val videoUri = clipData.getItemAt(i).uri
                            handleVideo(videoUri)
                        }
                    } else if (data.data != null) {
                        // 영상이 하나만 선택된 경우
                        val videoUri = data.data!!
                        handleVideo(videoUri)
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

    fun handleVideo(uri: Uri) {
        videos.add(uri)
    }
}