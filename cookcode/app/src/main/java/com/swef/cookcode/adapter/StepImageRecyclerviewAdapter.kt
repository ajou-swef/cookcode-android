package com.swef.cookcode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.R
import com.swef.cookcode.data.StepImageData
import com.swef.cookcode.databinding.StepImageRecyclerviewItemBinding

// Step에 들어갈 Image를 보여줄 RecyclerView를 위한 adapter
class StepImageRecyclerviewAdapter(
    // 갤러리에서 이미지를 선택할 수 있는 launcher는 activity에서 구현하여 어댑터에 넘겨준다
    private val pickImageLauncher: ActivityResultLauncher<String>,
    private val context: Context
    ): RecyclerView.Adapter<StepImageRecyclerviewAdapter.ViewHolder>() {

    // data는 StepImageData class에 정의되어있다
    var datas = mutableListOf<StepImageData>()
    val deleteImages = mutableListOf<String>()

    // view 접근에 용이하게 하기 위해 Viewbinding 사용
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StepImageRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    // step 생성시 recyclerview에 담긴 이미지 정보들 반환
    fun getData(): Array<String> {
        val uriData = mutableListOf<String>()
        for(index: Int in 0..2){
            if(datas[index].imageUri != null)
                uriData.add(datas[index].imageUri!!)
            }
        return uriData.toTypedArray()
    }

    // 이 명령어를 통해 recyclerview에 data를 넣어준다
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position], position)
    }

    // bind 함수에 data를 넘겨주는 함수
    inner class ViewHolder(
        private val binding: StepImageRecyclerviewItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepImageData, position: Int) {
            // 이미지 클릭 시 갤러리에서 이미지 불러올 수 있는 클릭 리스너
            binding.image.setOnClickListener {
                if(item.imageUri != null) {
                    // 이미 이미지가 등록된 상태면 팝업 메뉴를 통해 추가, 삭제 할 수 있음
                    showPopupMenu(binding.image, item, position)
                }
                else {
                    pickImageLauncher.launch("image/*")
                }
            }

            // image가 있으면 등록
            if (item.imageUri != null) {
                getImageFromUrl(item.imageUri!!)
            }
            // 없으면 기본 이미지 등록
            else {
                setBasicImageForGlide(item.basicImage)
            }
        }

        // 이미지 수정 삭제 팝업 메뉴
        private fun showPopupMenu(view: View, item: StepImageData, position: Int) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.image_popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    // 이미지 수정 버튼 클릭 시 이미지 불러오기
                    R.id.update_image -> {
                        deleteImages.add(item.imageUri!!)
                        item.imageUri = null
                        pickImageLauncher.launch("*/image")
                        true
                    }
                    // 이미지 삭제 버튼 클릭 시 해당 이미지 삭제 및 순서 당기기
                    R.id.delete_image -> {
                        deleteImages.add(item.imageUri!!)
                        item.imageUri = null
                        for(index: Int in position until datas.size - 1) {
                            // 해당 위치보다 뒤에 image가 있다면 앞으로 당겨오기
                            if (datas[index + 1].imageUri != null) {
                                datas[index].imageUri = datas[index + 1].imageUri
                                datas[index + 1].imageUri = null
                            }
                        }

                        notifyDataSetChanged()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun getImageFromUrl(imageUrl: String) {
            Glide.with(context)
                .load(imageUrl)
                .into(binding.image)

            binding.image.setBackgroundResource(0)
        }

        private fun setBasicImageForGlide(basicImage: Int) {
            Glide.with(context)
                .clear(binding.image)

            binding.image.setBackgroundResource(basicImage)
        }
    }
}