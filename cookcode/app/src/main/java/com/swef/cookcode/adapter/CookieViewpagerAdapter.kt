package com.swef.cookcode.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.swef.cookcode.R
import com.swef.cookcode.api.CookieAPI
import com.swef.cookcode.data.CommentData
import com.swef.cookcode.data.CookieData
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.CookiePreviewItemBinding
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
    private val context: Context
) : RecyclerView.Adapter<CookieViewpagerAdapter.ViewHolder>() {

    private val ERR_USER_CODE = -1

    var datas = mutableListOf<CookieData>()
    var hasNext = true
    var userId = ERR_USER_CODE

    lateinit var accessToken: String

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
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CookieData, position: Int) {
            binding.cookie.setBackgroundResource(R.drawable.loading_video_page)
            binding.progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                val videoUri = withContext(Dispatchers.IO) {
                    prepareVideo(item.videoUrl)
                }

                if (videoUri != null) {
                    binding.cookie.setBackgroundResource(0)
                    binding.cookie.setVideoURI(videoUri)
                    binding.progressBar.visibility = View.GONE

                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, videoUri)

                    val mediaController = MediaController(binding.cookie.context, false)
                    mediaController.setAnchorView(binding.cookie)
                    binding.cookie.setMediaController(mediaController)

                    binding.cookie.setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.start()
                    }

                    binding.cookie.setOnClickListener {
                        binding.cookie.start()
                    }
                }
            }

            binding.createdAt.text = item.createdAt
            binding.madeUser.text = item.madeUser.nickname
            binding.madeUserInBottomSheet.text = item.madeUser.nickname

            binding.cookieTitle.text = context.getString(
                R.string.string_shadow_convert, item.title)
            binding.cookieTitleInBottomSheet.text = item.title

            binding.description.text = item.description

            initLikeButton(item.isLiked)

            binding.btnLike.setOnClickListener {
                putLikeStateCookie(item, position)
            }

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

            val commentRecyclerviewAdapter = CommentRecyclerviewAdapter(context)
            initCommentRecyclerview(commentRecyclerviewAdapter, item.comments)

            if (item.comments.isNotEmpty())
                binding.noExistComments.visibility = View.GONE
        }

        private fun initCommentRecyclerview(recyclerViewAdapter: CommentRecyclerviewAdapter, commentDatas: List<CommentData>) {
            binding.commentRecyclerview.apply {
                adapter = recyclerViewAdapter
                layoutManager = LinearLayoutManagerWrapper(context, LinearLayout.VERTICAL, false)
            }

            recyclerViewAdapter.userId = userId
            recyclerViewAdapter.accessToken = accessToken
            recyclerViewAdapter.datas = commentDatas as MutableList<CommentData>
            recyclerViewAdapter.notifyItemRangeChanged(0, commentDatas.size - 1)
        }

        private fun initLikeButton(like: Boolean) {
            if (like){
                binding.btnLike.setBackgroundResource(R.drawable.icon_liked)
            }
            else {
                binding.btnLike.setBackgroundResource(R.drawable.icon_unliked)
            }
        }

        private fun putLikeStateCookie(item: CookieData, position: Int) {
            API.putLikeCookie(accessToken, item.cookieId).enqueue(object: Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful){
                        if(item.isLiked) {
                            binding.btnLike.setBackgroundResource(R.drawable.icon_unliked)
                            item.isLiked = false
                            item.likeNumber--
                        }
                        else {
                            binding.btnLike.setBackgroundResource(R.drawable.icon_liked)
                            item.isLiked = true
                            item.likeNumber++
                        }

                        notifyItemChanged(position)
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

                Log.d("data_size", tempFile.absolutePath)

                return@withContext Uri.parse(tempFile.absolutePath)
            }

            return@withContext null
        }
    }

    fun putToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}