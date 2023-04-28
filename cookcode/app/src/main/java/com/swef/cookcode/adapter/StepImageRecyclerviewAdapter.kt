package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.StepImageData
import com.swef.cookcode.databinding.StepImageRecyclerviewItemBinding


// Step에 들어갈 Image를 보여줄 RecyclerView를 위한 adapter
class StepImageRecyclerviewAdapter(
    // 갤러리에서 이미지를 선택할 수 있는 launcher는 activity에서 구현하여 어댑터에 넘겨준다
    private val pickImageLauncher: ActivityResultLauncher<String>
    ): RecyclerView.Adapter<StepImageRecyclerviewAdapter.ViewHolder>() {

    // data는 StepImageData class에 정의되어있다
    var datas = mutableListOf<StepImageData>()

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
        for(i: Int in 0..2){
            if(datas[i].imageUri != null)
                uriData.add(datas[i].imageUri.toString())
            }
        return uriData.toTypedArray()
    }

    // 이 명령어를 통해 recyclerview에 data를 넣어준다
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    // bind 함수에 data를 넘겨주는 함수
    inner class ViewHolder(
        private val binding: StepImageRecyclerviewItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepImageData) {
            // 이미지 클릭 시 갤러리에서 이미지 불러올 수 있는 클릭 리스너
            binding.image.setOnClickListener {
                if(item.imageUri != null) {
                    // 이미 이미지가 등록된 상태면 팝업 메뉴를 통해 추가, 삭제 할 수 있음
                    showPopupMenu(binding.image, item)
                }
                else {
                    pickImageLauncher.launch("*/image")
                }
            }

            // image가 있으면 등록
            if (item.imageUri != null) {
                binding.image.setBackgroundResource(0)
                binding.image.setImageURI(item.imageUri)
            }
            // 없으면 기본 이미지 등록
            else {
                binding.image.setImageURI(null)
                binding.image.setBackgroundResource(item.basicImage)
            }
        }

        // 이미지 추가 삭제 팝업 메뉴
        private fun showPopupMenu(view: View, item: StepImageData) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.image_popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    // 이미지 추가 버튼 클릭 시 이미지 불러오기
                    R.id.update_image -> {
                        pickImageLauncher.launch("*/image")
                        true
                    }
                    // 이미지 삭제 버튼 클릭 시 imageUri 삭제 및 기본 이미지로 변경
                    R.id.delete_image -> {
                        item.imageUri = null
                        binding.image.setImageURI(null)
                        binding.image.setBackgroundResource(item.basicImage)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}