package com.swef.cookcode

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import com.swef.cookcode.data.GlobalVariables.ERR_CODE
import com.swef.cookcode.data.GlobalVariables.cookieAPI
import com.swef.cookcode.data.response.OneCookieResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityCookieModifyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CookieModifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCookieModifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCookieModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cookieId = intent.getIntExtra("cookie_id", ERR_CODE)

        getCookieDataFromCookieId(cookieId)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        binding.cookieModify.setOnClickListener {
            val body = HashMap<String, String>()
            body["title"] = binding.editCookieName.text.toString()
            body["desc"] = binding.editDescription.text.toString()

            updateCookie(cookieId, body)
        }
    }

    private fun getCookieDataFromCookieId(cookieId: Int) {
        cookieAPI.getCookie(cookieId).enqueue(object : Callback<OneCookieResponse> {
            override fun onResponse(
                call: Call<OneCookieResponse>,
                response: Response<OneCookieResponse>
            ) {
                if (response.isSuccessful){
                    val data = response.body()!!.data
                    val title = data.title
                    val description = data.description
                    val videoUrl = data.videoUrl

                    binding.editCookieName.setText(title)
                    binding.editDescription.setText(description)
                    binding.combinedVideo.setVideoURI(Uri.parse(videoUrl))

                    val mediaController = MediaController(binding.combinedVideo.context, false)
                    mediaController.setAnchorView(binding.combinedVideo)
                    binding.combinedVideo.setMediaController(mediaController)

                    binding.combinedVideo.setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.start()
                    }

                    binding.combinedVideo.setOnClickListener {
                        binding.combinedVideo.start()
                    }
                }
                else {
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                }
            }

            override fun onFailure(call: Call<OneCookieResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun updateCookie(cookieId: Int, body: HashMap<String, String>){
        cookieAPI.patchCookie(cookieId, body).enqueue(object : Callback<StatusResponse>{
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {
                if (response.isSuccessful){
                    putToastMessage("정상적으로 수정되었습니다.")
                    val intent = Intent(this@CookieModifyActivity, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                else {
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun putToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}