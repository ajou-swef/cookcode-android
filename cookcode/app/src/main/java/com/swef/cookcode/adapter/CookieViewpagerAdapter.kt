package com.swef.cookcode.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.swef.cookcode.CookieModifyActivity
import com.swef.cookcode.LinearLayoutManagerWrapper
import com.swef.cookcode.R
import com.swef.cookcode.UserPageActivity
import com.swef.cookcode.api.CookieAPI
import com.swef.cookcode.data.CommentData
import com.swef.cookcode.data.CookieData
import com.swef.cookcode.data.response.Comment
import com.swef.cookcode.data.response.CommentResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.CookiePreviewItemBinding
import com.swef.cookcode.`interface`.CommentOnClickListener
import com.swef.cookcode.`interface`.CookieDeleteListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CookieViewpagerAdapter(
    private val context: Context,
    private val listener: CookieDeleteListener
) : RecyclerView.Adapter<CookieViewpagerAdapter.ViewHolder>() {

    private val ERR_USER_CODE = -1

    var datas = mutableListOf<CookieData>()
    var hasNext = true
    var userId = ERR_USER_CODE

    lateinit var accessToken: String
    lateinit var refreshToken: String

    private val API = CookieAPI.create()

    private lateinit var commentBottomSheetCallback: BottomSheetBehavior.BottomSheetCallback
    private lateinit var infoBottomSheetBehavior: BottomSheetBehavior.BottomSheetCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CookiePreviewItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position], position)
    }

    inner class ViewHolder(
        private val binding: CookiePreviewItemBinding
    ): RecyclerView.ViewHolder(binding.root), CommentOnClickListener {
        private lateinit var commentRecyclerviewAdapter: CommentRecyclerviewAdapter
        fun bind(item: CookieData, position: Int) {
            binding.cookie.setBackgroundResource(R.drawable.loading_video_page)
            binding.progressBar.visibility = View.VISIBLE

            initModifyDeleteButton(item.cookieId, position)

            CoroutineScope(Dispatchers.Main).launch {
                val videoUri = withContext(Dispatchers.IO) {
                    prepareVideo(item.videoUrl)
                }

                if (videoUri != null) {
                    binding.cookie.setBackgroundResource(0)
                    binding.cookie.setVideoURI(videoUri)
                    binding.progressBar.visibility = View.GONE

                    val mediaController = MediaController(binding.cookie.context, false)
                    mediaController.setAnchorView(binding.cookie)
                    binding.cookie.setMediaController(mediaController)

                    binding.cookie.setOnPreparedListener { mediaPlayer ->
                        val videoWidth = mediaPlayer.videoWidth
                        val videoHeight = mediaPlayer.videoHeight
                        resizeVideoView(binding.cookie, videoWidth, videoHeight)
                        mediaPlayer.start()
                    }

                    binding.cookie.setOnClickListener {
                        binding.cookie.start()
                    }
                }
            }

            binding.createdAt.text = item.createdAt.substring(0 until 10)
            binding.madeUser.text = item.madeUser.nickname
            binding.madeUserInBottomSheet.text = item.madeUser.nickname

            binding.cookieTitle.text = context.getString(
                R.string.string_shadow_convert, item.title)
            binding.cookieTitleInBottomSheet.text = item.title

            binding.description.text = item.description

            initLikeButton(item.isLiked)

            binding.btnLike.setOnClickListener {
                putLikeStateCookie(item)
            }

            binding.commentNumber.text = item.commentCount.toString()
            binding.likeNumber.text = item.likeNumber.toString()

            initCommentBottomSheetCallback()
            initInfoBottomSheetCallback()

            val commentBottomSheet = BottomSheetBehavior.from(binding.commentBottomSheet)
            commentBottomSheet.addBottomSheetCallback(commentBottomSheetCallback)
            initCommentBottomSheetListener(commentBottomSheet)

            binding.btnComment.setOnClickListener {
                commentBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }

            val infoBottomSheet = BottomSheetBehavior.from(binding.infoBottomSheet)
            infoBottomSheet.addBottomSheetCallback(infoBottomSheetBehavior)
            initInfoBottomSheetListener(infoBottomSheet)

            binding.btnMore.setOnClickListener {
                infoBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }
            binding.cookieTitle.setOnClickListener {
                infoBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }

            commentRecyclerviewAdapter = CommentRecyclerviewAdapter(context, "cookie", this)
            initCommentRecyclerview(item.cookieId)

            binding.btnConfirm.setOnClickListener {
                commentRecyclerviewAdapter.datas.clear()
                val comment = binding.editComment.text.toString()
                putCommentCookie(item.cookieId, comment)
            }

            binding.madeUserInBottomSheet.setOnClickListener {
                startUserPageActivity(item.madeUser.userId)
            }
            binding.madeUser.setOnClickListener {
                startUserPageActivity(item.madeUser.userId)
            }
        }

        private fun initCommentRecyclerview(cookieId: Int) {
            binding.commentRecyclerview.apply {
                adapter = commentRecyclerviewAdapter
                layoutManager = LinearLayoutManagerWrapper(context, LinearLayout.VERTICAL, false)
            }

            getCookieComments(cookieId)
        }

        private fun getCookieComments(cookieId: Int){
            API.getCookieComments(accessToken, cookieId).enqueue(object: Callback<CommentResponse>{
                override fun onResponse(
                    call: Call<CommentResponse>,
                    response: Response<CommentResponse>
                ) {
                    if (response.isSuccessful) {
                        val comments = getCommentFromResponse(response.body()!!.content.comments)

                        commentRecyclerviewAdapter.userId = userId
                        commentRecyclerviewAdapter.accessToken = accessToken
                        commentRecyclerviewAdapter.cookieId = cookieId

                        if (comments.isEmpty()) {
                            binding.noExistComments.visibility = View.VISIBLE
                            commentRecyclerviewAdapter.notifyDataSetChanged()
                        }
                        else {
                            if (commentRecyclerviewAdapter.datas.isEmpty()) {
                                commentRecyclerviewAdapter.datas = comments as MutableList<CommentData>
                                commentRecyclerviewAdapter.notifyItemRangeChanged(0, comments.size)
                            }
                            else {
                                val beforeSize = commentRecyclerviewAdapter.itemCount
                                commentRecyclerviewAdapter.datas.addAll(comments)
                                commentRecyclerviewAdapter.notifyItemRangeChanged(beforeSize, beforeSize + comments.size)
                            }
                            binding.noExistComments.visibility = View.GONE
                        }
                    }
                    else {
                        putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                        Log.d("data_size", response.errorBody()!!.string())
                        Log.d("data_size", call.request().toString())
                    }
                }

                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    Log.d("data_size", t.message.toString())
                    Log.d("data_size", call.request().toString())
                    putToastMessage("잠시 후 다시 시도해주세요.")
                }
            })
        }

        private fun getCommentFromResponse(response: List<Comment>): List<CommentData>{
            val comments = mutableListOf<CommentData>()

            for (item in response){
                comments.apply {
                    add(CommentData(item.madeUser, item.comment, item.commentId))
                }
            }

            return comments
        }

        private fun initLikeButton(like: Int) {
            if (like == 1){
                binding.btnLike.setBackgroundResource(R.drawable.icon_liked)
            }
            else {
                binding.btnLike.setBackgroundResource(R.drawable.icon_unliked)
            }
        }

        private fun initModifyDeleteButton(cookieId: Int, position: Int) {
            binding.btnModify.setOnClickListener {
                val intent = Intent(context, CookieModifyActivity::class.java)
                intent.putExtra("access_token", accessToken)
                intent.putExtra("refresh_token", refreshToken)
                intent.putExtra("user_id", userId)
                intent.putExtra("cookie_id", cookieId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }

            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle("쿠키 삭제")
                    setMessage("정말 삭제 하시겠습니까?")
                    setPositiveButton("삭제") { _, _ ->
                        deleteCookie(cookieId)
                        listener.itemDeleted()
                        listener.itemDeletedAt(position)
                    }
                    setNegativeButton("취소") { _, _ -> /* Do nothing */ }
                    show()
                }
            }
        }

        private fun deleteCookie(cookieId: Int){
            API.deleteCookie(accessToken, cookieId).enqueue(object : Callback<StatusResponse>{
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful){
                        notifyDataSetChanged()
                    }
                    else {
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", response.errorBody()!!.string())
                        putToastMessage("에러 발생!")
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", t.message.toString())
                    putToastMessage("잠시 후 다시 시도해주세요.")
                }
            })
        }

        private fun putCommentCookie(cookieId: Int, comment: String) {
            val body = HashMap<String, String>()
            body["comment"] = comment
            API.putCookieComment(accessToken, cookieId, body).enqueue(object : Callback<StatusResponse>{
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful) {
                        putToastMessage("댓글이 정상적으로 등록되었습니다.")
                        getCookieComments(cookieId)
                    }
                    else {
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", response.errorBody()!!.string())
                        putToastMessage("에러 발생!")
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", t.message.toString())
                    putToastMessage("잠시 후 다시 시도해주세요.")
                }

            })
        }

        private fun putLikeStateCookie(item: CookieData) {
            API.putLikeCookie(accessToken, item.cookieId).enqueue(object: Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful){
                        if(item.isLiked == 1) {
                            binding.btnLike.setBackgroundResource(R.drawable.icon_unliked)
                            item.isLiked = 0
                            item.likeNumber--

                        }
                        else {
                            binding.btnLike.setBackgroundResource(R.drawable.icon_liked)
                            item.isLiked = 1
                            item.likeNumber++
                        }
                        binding.likeNumber.text = item.likeNumber.toString()
                    }
                    else {
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", response.errorBody()!!.string())
                        putToastMessage("에러 발생!")
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", t.message.toString())
                    putToastMessage("잠시 후 다시 시도해주세요.")
                }
            })
        }

        private fun initCommentBottomSheetCallback() {
            commentBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                // bottom sheet의 상태값 변경
                override fun onStateChanged(bottomSheet: View, newState: Int) { /* Do Nothing */ }

                // botton sheet가 스크롤될 때 호출
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset >= 0) {
                        binding.guideArrow.rotation = (1 - slideOffset) * 180F - 90F
                    }
                }
            }
            binding.commentBottomSheet.bringToFront()
        }

        private fun initCommentBottomSheetListener(commentBottomSheet: BottomSheetBehavior<ConstraintLayout>) {
            binding.commentBottomSheet.setOnClickListener {
                if (commentBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
                    commentBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        private fun initInfoBottomSheetCallback() {
            infoBottomSheetBehavior = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) { /* Do Nothing */ }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset >= 0) {
                        binding.guideArrowInfo.rotation = (1 - slideOffset) * 180F - 90F
                    }
                }
            }


            binding.infoBottomSheet.bringToFront()
        }

        private fun initInfoBottomSheetListener(infoBottomSheet: BottomSheetBehavior<ConstraintLayout>) {
            binding.infoBottomSheet.setOnClickListener {
                if (infoBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
                    infoBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        private fun startUserPageActivity(madeUserId: Int) {
            val nextIntent = Intent(context, UserPageActivity::class.java)
            nextIntent.putExtra("access_token", accessToken)
            nextIntent.putExtra("refresh_token", refreshToken)
            nextIntent.putExtra("my_user_id", userId)
            nextIntent.putExtra("user_id", madeUserId)
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(nextIntent)
        }

        private suspend fun prepareVideo(videoUrl: String): Uri? = withContext(Dispatchers.IO) {
            return@withContext downloadVideo(videoUrl)
        }
        private suspend fun downloadVideo(videoUrl: String): Uri? = withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .url(videoUrl)
                .build()

            val response = client.newCall(request).execute()
            val responseBody: ResponseBody? = response.body

            if (response.isSuccessful && responseBody != null) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                val tempFile = File.createTempFile("VIDEO_$timeStamp", ".mp4", cacheDir)

                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(tempFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                return@withContext Uri.parse(tempFile.absolutePath)
            }

            return@withContext null
        }

        override fun itemOnClick(id: Int) {
            commentRecyclerviewAdapter.datas.clear()
            getCookieComments(id)
        }

        private fun resizeVideoView(videoView: VideoView, videoWidth: Int, videoHeight: Int) {
            val viewWidth = videoView.width
            val viewHeight = videoView.height
            val widthRatio = viewWidth.toFloat() / videoWidth.toFloat()
            val heightRatio = viewHeight.toFloat() / videoHeight.toFloat()

            val scaleRatio = if (widthRatio < heightRatio) widthRatio else heightRatio

            val finalWidth = (videoWidth * scaleRatio).toInt()
            val finalHeight = (videoHeight * scaleRatio).toInt()

            // VideoView의 크기를 조정하여 비율을 유지합니다.
            val layoutParams = videoView.layoutParams
            layoutParams.width = finalWidth
            layoutParams.height = finalHeight
            videoView.layoutParams = layoutParams
        }
    }

    fun putToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}