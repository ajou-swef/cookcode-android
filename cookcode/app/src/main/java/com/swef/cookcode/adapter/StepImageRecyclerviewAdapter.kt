package com.swef.cookcode.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
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
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    // 이 명령어를 통해 recyclerview에 data를 넣어준다
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
        Log.d("data", "success")
    }

    // bind 함수에 data를 넘겨주는 함수
    inner class ViewHolder(
        private val binding: StepImageRecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepImageData){
            // 이미지 클릭 시 갤러리에서 이미지 불러올 수 있는 클릭 리스너
            binding.image.setOnClickListener {
                pickImageLauncher.launch("*/image")
            }

            // image가 있으면 등록
            if(item.imageUri != null) {
                binding.image.setImageURI(item.imageUri)
            }
            // 없으면 기본 이미지 등록
            else {
                binding.image.setBackgroundResource(item.basicImage)
            }
        }
    }
}