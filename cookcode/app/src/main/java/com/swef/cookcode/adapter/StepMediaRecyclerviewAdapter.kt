package com.swef.cookcode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.StepMediaData
import com.swef.cookcode.databinding.StepMediaRecyclerviewItemBinding

class StepMediaRecyclerviewAdapter(
    private val context: Context, mediaUris: List<StepMediaData>
): RecyclerView.Adapter<StepMediaRecyclerviewAdapter.ViewHolder>() {

    private lateinit var binding: StepMediaRecyclerviewItemBinding

    private val datas = mediaUris

    // 현재 재생중인 비디오 뷰 정보
    private var currentVideoView: VideoView? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StepMediaRecyclerviewAdapter.ViewHolder {
        binding = StepMediaRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: StepMediaRecyclerviewAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: StepMediaRecyclerviewItemBinding, private val context: Context
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepMediaData){
            // media가 image인지, video인지에 따라서 뷰의 모양이 달라짐
            if(item.type == "image"){
                // image일 경우 단순하게 이미지만 보여줌
                binding.videoView.visibility = View.INVISIBLE
                binding.imageView.visibility = View.VISIBLE
                binding.imageView.setImageURI(item.mediaData)
                stopCurrentVideo()
            }
            else {
                // video일 경우 play 버튼 클릭시 영상 재생
                binding.videoView.visibility = View.VISIBLE
                binding.imageView.visibility = View.INVISIBLE

                // 재생, 일시정지 등 버튼 붙이기
                val mediaController = MediaController(context, false)
                mediaController.setAnchorView(binding.videoView)
                binding.videoView.setMediaController(mediaController)

                binding.videoView.setVideoURI(item.mediaData)
                // video가 준비되면 실행
                binding.videoView.setOnPreparedListener { mp ->
                    stopCurrentVideo()
                    mp.start()
                }
            }
        }
    }

    fun stopCurrentVideo() {
        // 다른 영상이나 이미지로 넘어갈때 현재 재생 중인 동영상이 있으면 정지시킴
        currentVideoView?.stopPlayback()
        // 현재 재생 중인 동영상을 저장
        currentVideoView = binding.videoView
    }

}