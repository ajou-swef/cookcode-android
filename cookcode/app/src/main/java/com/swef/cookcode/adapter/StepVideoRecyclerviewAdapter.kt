package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.StepVideoData
import com.swef.cookcode.databinding.StepVideoRecyclerviewItemBinding

class StepVideoRecyclerviewAdapter(
    private val pickVideoLauncher: ActivityResultLauncher<String>
    ): RecyclerView.Adapter<StepVideoRecyclerviewAdapter.ViewHolder>() {

    var datas = mutableListOf<StepVideoData>()
    val deleteVideos = mutableListOf<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StepVideoRecyclerviewAdapter.ViewHolder {
        val binding = StepVideoRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    fun getData(): Array<String> {
        val uriData = mutableListOf<String>()
        for(index: Int in 0..1){
            if(datas[index].uri != null)
                uriData.add(datas[index].uri.toString())
        }
        return uriData.toTypedArray()
    }

    override fun onBindViewHolder(holder: StepVideoRecyclerviewAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(
        private val binding: StepVideoRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepVideoData) {
            binding.video.setOnClickListener {
                if(item.uri != null) {
                    showPopupMenu(binding.video, item)
                }
                else {
                    pickVideoLauncher.launch("video/*")
                }
            }


            if(item.thumbnail != null){
                binding.video.setBackgroundResource(0)
                binding.video.setImageBitmap(item.thumbnail)
            }
            else {
                binding.video.setImageBitmap(null)
                binding.video.setBackgroundResource(R.drawable.upload_image)
            }
        }

        private fun showPopupMenu(view: View, item: StepVideoData) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.video_popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    // 동영상 추가 버튼 클릭 시 동영상 불러오기
                    R.id.update_video -> {
                        pickVideoLauncher.launch("video/*")
                        deleteVideos.add(item.uri!!)
                        true
                    }
                    // 동영상 삭제 버튼 클릭 시 영상 정보 삭제 및 기본 이미지로 변경
                    R.id.delete_video -> {
                        deleteVideos.add(item.uri!!)
                        item.thumbnail = null
                        item.uri = null
                        binding.video.setImageBitmap(null)
                        binding.video.setBackgroundResource(R.drawable.upload_image)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}