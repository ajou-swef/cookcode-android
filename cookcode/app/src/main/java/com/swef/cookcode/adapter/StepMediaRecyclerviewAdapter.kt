package com.swef.cookcode.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.data.StepMediaData
import com.swef.cookcode.databinding.PreviewImageItemBinding
import com.swef.cookcode.databinding.PreviewVideoItemBinding

class StepMediaRecyclerviewAdapter(
    mediaUris: List<StepMediaData>,
    private val context: Context
): RecyclerView.Adapter<StepMediaRecyclerviewAdapter.MediaViewHolder>() {

    // Image인지 Video인지에 따라 불러오는 viewbinding layout이 다르기 때문에 타입 연산자 지정
    companion object {
        private const val TYPE_IMAGE = 1
        private const val TYPE_VIDEO = 2
    }

    private val datas = mediaUris

    // Data class에 명시한 type 변수로 image, video 구별
    override fun getItemViewType(position: Int): Int {
        return if (datas[position].type == "image") TYPE_IMAGE else TYPE_VIDEO
    }

    // type 구별에 따라 item layout view binding을 다르게 해준다
    // imageview, videoview는 다른 뷰기 때문에 하나의 레이아웃에 넣을 경우 재사용이 어려움
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val binding = PreviewImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ImageViewHolder(binding)
        }
        else {
            val binding = PreviewVideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            VideoViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val data = datas[position]
        if (holder is ImageViewHolder) {
            getImageFromUrl(data.mediaData, holder.binding.imageView)
            // stopCurrentVideo()
        }
        else if (holder is VideoViewHolder) {
            // 가져올 video의 uri를 가져옴
            val uri = data.mediaData

            // 현재 재생 중인 videoView의 tag(uri)가 다르다면 비디오 중지
            if (uri != holder.binding.videoView.tag) {
                // 재생, 일시정지 등 버튼 붙이기
                val mediaController = MediaController(holder.binding.videoView.context, false)
                mediaController.setAnchorView(holder.binding.videoView)
                holder.binding.videoView.setMediaController(mediaController)

                // 뷰 생성시 이전에 재생 중이던 비디오 중지
                holder.binding.videoView.stopPlayback()

                holder.binding.videoView.setVideoURI(Uri.parse(uri))
                // tag에 현재 videoView의 uri를 저장해두어 무한 로딩을 방지함
                holder.binding.videoView.tag = uri

                // video가 준비되면 실행
                holder.binding.videoView.setOnPreparedListener { mp ->
                    Log.d("data_size", "success")
                    mp.start()
                }
            }
        }
    }

    override fun getItemCount(): Int = datas.size
    open class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ImageViewHolder(val binding: PreviewImageItemBinding) : MediaViewHolder(binding.root)
    class VideoViewHolder(val binding: PreviewVideoItemBinding) : MediaViewHolder(binding.root)

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .into(view)
    }
}