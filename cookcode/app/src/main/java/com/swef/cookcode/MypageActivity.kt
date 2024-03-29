package com.swef.cookcode

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.swef.cookcode.data.GlobalVariables.accountAPI
import com.swef.cookcode.data.GlobalVariables.authority
import com.swef.cookcode.data.GlobalVariables.userId
import com.swef.cookcode.data.response.ProfileImageResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityMypageBinding
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

class MypageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageBinding

    private var profileImage : String? = null

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileImage = intent.getStringExtra("profile_image")
        if (profileImage != null) {
            getImageFromUrl(profileImage!!, binding.profileImage)
        }

        initGalleryLauncher()

        val userName = intent.getStringExtra("user_name")
        binding.userName.text = userName

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        initButtonByAuthority()

        binding.myContents.setOnClickListener {
            startMyContentActivity()
        }

        binding.subscribedUsers.setOnClickListener {
            startMySubscriberActivity()
        }

        binding.profileImage.setOnClickListener {
            showPopupMenuForProfile()
        }

        binding.modifyPw.setOnClickListener {
            startPasswordModifyActivity()
        }

        binding.requestAuthority.setOnClickListener {
            showRequestAuthorityAlertDialog()
        }

        binding.myPremiumMembershipModify.setOnClickListener {
            startMembershipCreateActivity()
        }

        binding.premiumMembershipManage.setOnClickListener {
            startMembershipManageActivity()
        }

        // 로그아웃
        binding.logout.setOnClickListener { buildAlertDialog("logout") }

        // 계정 삭제
        binding.btnDeleteUser.setOnClickListener { buildAlertDialog("delete") }
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .into(view)
    }

    private fun showPopupMenuForProfile() {
        val popupMenu = PopupMenu(this, binding.profileImage)
        popupMenu.menuInflater.inflate(R.menu.image_popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.update_image -> {
                    galleryLauncher.launch(galleryIntent)
                    true
                }
                R.id.delete_image -> {
                    if (profileImage != null) {
                        patchProfileImage(null, makeStringMultipartBody(profileImage!!))
                        putToastMessage("삭제가 완료되었습니다.")
                    }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun initGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data

                if (imageUri != null) {
                    val imageFile = makeImageMultipartBody(imageUri)
                    var oldImageFile: MultipartBody.Part? = null
                    if (profileImage != null) {
                        oldImageFile = makeStringMultipartBody(profileImage!!)
                    }

                    patchProfileImage(imageFile, oldImageFile)
                    putToastMessage("변경이 완료되었습니다.")
                }
            }
        }
    }

    private fun patchProfileImage(profileImage: MultipartBody.Part?, oldProfileImage: MultipartBody.Part?) {
        val formData = mutableListOf<MultipartBody.Part>()

        formData.add(profileImage ?: makeNullMultipartBody("profileImage"))
        formData.add(oldProfileImage ?: makeNullMultipartBody("oldProfileImage"))

        accountAPI.patchProfileImage(formData).enqueue(object : Callback<ProfileImageResponse> {
            override fun onResponse(
                call: Call<ProfileImageResponse>,
                response: Response<ProfileImageResponse>
            ) {
                if (response.isSuccessful) {
                    val imageUrl = response.body()!!.data.url[0]

                    if (imageUrl != null) {
                        getImageFromUrl(imageUrl, binding.profileImage)
                    }
                    else {
                        binding.profileImage.setImageResource(R.drawable.user_profile)
                    }
                }
                else {
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<ProfileImageResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun makeImageMultipartBody(uri: Uri): MultipartBody.Part {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "image.png") // 임시 파일 생성

        val outputStream: OutputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream) // 이미지를 임시 파일로 복사
        inputStream?.close()
        outputStream.close()

        val requestBody = file.asRequestBody("image/png".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("profileImage", file.name, requestBody)
    }

    private fun makeStringMultipartBody(url: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData("oldProfileImage", url)
    }

    private fun makeNullMultipartBody(key: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(key, "")
    }

    private fun initButtonByAuthority() {
        when (authority) {
            "USER" -> {
                binding.earningsCheck.visibility = View.GONE
                binding.myPremiumMembershipModify.visibility = View.GONE
            }
            "INFLUENCER" -> {
                binding.requestAuthority.visibility = View.GONE
                binding.premiumMembershipManage.setBackgroundResource(R.drawable.half_rounded_lower_component)
            }
        }
    }

    private fun startMyContentActivity() {
        val nextIntent = Intent(this, UserPageActivity::class.java)
        nextIntent.putExtra("user_id", userId)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(nextIntent)
    }

    private fun startMySubscriberActivity() {
        val nextIntent = Intent(this, SubscriberActivity::class.java)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(nextIntent)
    }

    private fun startPasswordModifyActivity() {
        val nextIntent = Intent(this, PasswordModifyActivity::class.java)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(nextIntent)
    }

    private fun startMembershipCreateActivity() {
        val nextIntent = Intent(this, MyMembershipCreateActivity::class.java)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(nextIntent)
    }

    private fun startMembershipManageActivity() {
        val nextIntent = Intent(this, SignedMembershipActivity::class.java)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(nextIntent)
    }

    private fun showRequestAuthorityAlertDialog() {
        return AlertDialog.Builder(this)
            .setMessage("인플루언서 권한을 요청하시겠습니까?\n(최소 구독자 10명 이상부터 가능합니다.)")
            .setPositiveButton("확인"
            ) { _, _ ->
                accountAPI.patchAuthority().enqueue(object : Callback<StatusResponse> {
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()!!.status == 200) {
                                putToastMessage("권한 신청이 완료 되었습니다.")
                            }
                        } else {
                            putToastMessage("신청 조건이 만족되지 않았습니다.")
                            Log.d("data_size", call.request().toString())
                            Log.d("data_size", response.errorBody()!!.string())
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        putToastMessage("잠시 후 다시 시도해주세요.")
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", t.message.toString())
                    }
                })

            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }

    private fun buildAlertDialog(type: String) {
        val message: String
        val toastMessage: String

        if (type == "logout") {
            message = "정말 로그아웃 하시겠습니까?"
            toastMessage = "정상적으로 로그아웃 되었습니다."
        }
        else {
            message = "정말 계정을 삭제 하시겠습니까?"
            toastMessage = "정상적으로 삭제 되었습니다."
        }

        return AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("확인"
            ) { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                if (type == "logout") {
                    startActivity(intent)
                }
                else {
                    accountAPI.patchAccount().enqueue(object : Callback<StatusResponse> {
                        override fun onResponse(
                            call: Call<StatusResponse>,
                            response: Response<StatusResponse>
                        ) {
                            if (response.isSuccessful) {
                                putToastMessage(toastMessage)
                                startActivity(intent)
                            } else {
                                putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                                Log.d("data_size", response.errorBody()!!.string())
                            }
                        }

                        override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                            putToastMessage("잠시 후 다시 시도해주세요.")
                            Log.d("data_size", call.request().toString())
                            Log.d("data_size", t.message.toString())
                        }
                    })
                }
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}