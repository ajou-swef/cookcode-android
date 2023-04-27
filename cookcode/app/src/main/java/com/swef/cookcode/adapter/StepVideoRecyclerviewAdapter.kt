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

    override fun onBindViewHolder(holder: StepVideoRecyclerviewAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(
        private val binding: StepVideoRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepVideoData) {
            binding.video.setOnClickListener {
                binding.video.setOnClickListener {
                    if(item.uri != null) {
                        showPopupMenu(binding.video, item)
                    }
                    else {
                        pickVideoLauncher.launch("*/video")
                    }
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
            popupMenu.menuInflater.inflate(R.menu.image_popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    // 이미지 추가 버튼 클릭 시 이미지 불러오기
                    R.id.update_image -> {
                        pickVideoLauncher.launch("*/image")
                        true
                    }
                    // 이미지 삭제 버튼 클릭 시 imageUri 삭제 및 기본 이미지로 변경
                    R.id.delete_image -> {
                        item.uri = null
                        binding.video.setImageURI(null)
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