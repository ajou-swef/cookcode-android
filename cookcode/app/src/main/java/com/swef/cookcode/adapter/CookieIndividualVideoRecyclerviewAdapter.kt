package com.swef.cookcode.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.VideoData
import com.swef.cookcode.databinding.CookieVideoAddItemBinding
import com.swef.cookcode.databinding.IndividualCookieVideoItemBinding
import com.swef.cookcode.`interface`.VideoOnClickListener

class CookieIndividualVideoRecyclerviewAdapter(
    private val galleryLauncher: ActivityResultLauncher<Intent>,
    private val galleryStartIntent: Intent,
    private val onClickListener: VideoOnClickListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_VIDEO = 1
        private const val VIEW_TYPE_ADD = 2
    }

    var datas = mutableListOf<VideoData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_VIDEO) {
            val binding = IndividualCookieVideoItemBinding.inflate(LayoutInflater.from(parent.context),  parent, false)
            VideoViewHolder(binding)
        } else {
            val binding = CookieVideoAddItemBinding.inflate(LayoutInflater.from(parent.context),  parent, false)
            AddViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VideoViewHolder) {
            val videoView = holder.binding.selectedVideo
            val redCross = holder.binding.redCross
            videoView.setImageBitmap(datas[position].thumbnail)
            videoView.setOnClickListener{onClickListener.onItemDelete(position)}
            redCross.setOnClickListener{onClickListener.onItemDelete(position)}
        }
        else if (holder is AddViewHolder) {
            holder.binding.addVideo.setOnClickListener{
                galleryLauncher.launch(galleryStartIntent)
            }
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = datas[position]
        return if (currentItem.uri == null) {
            VIEW_TYPE_ADD
        } else {
            VIEW_TYPE_VIDEO
        }
    }

    fun setData(videoData: VideoData){
        for ((index, item) in datas.withIndex()){
            if (item.uri == null){
                datas.add(index, videoData)
                notifyItemInserted(index)
                notifyItemRangeChanged(index, datas.size)

                break
            }
        }
    }


    inner class VideoViewHolder(val binding: IndividualCookieVideoItemBinding) : RecyclerView.ViewHolder(binding.root) {}
    inner class AddViewHolder(val binding: CookieVideoAddItemBinding) : RecyclerView.ViewHolder(binding.root) {}

}