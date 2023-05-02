package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.StepMediaData
import com.swef.cookcode.databinding.StepPreviewRecyclerviewItemBinding

class StepPreviewRecyclerviewAdapter(
    stepData: List<StepData>
) : RecyclerView.Adapter<StepPreviewRecyclerviewAdapter.ViewHolder>() {

    private lateinit var binding: StepPreviewRecyclerviewItemBinding

    // step 정보
    private val datas = stepData
    // 각 스텝 별 이미지, 영상 uri list
    private val mediaUris = mutableListOf<List<StepMediaData>>()

    lateinit var stepMediaRecyclerviewAdapter: StepMediaRecyclerviewAdapter

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StepPreviewRecyclerviewAdapter.ViewHolder {
        binding = StepPreviewRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: StepPreviewRecyclerviewAdapter.ViewHolder,
        position: Int
    ) {
        makeMediaUriList()
        holder.bind(datas[position], mediaUris[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: StepPreviewRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepData, uri: List<StepMediaData>){
            // step에 대한 정보를 view에 넣어줌
            binding.stepNumber.text = item.numberOfStep.toString() + " 단계 : "
            binding.stepTitle.text = item.title
            binding.stepDescription.text = item.description

            // step 이미지나 영상을 위한 adapter
            stepMediaRecyclerviewAdapter = StepMediaRecyclerviewAdapter(uri)

            // viewpager에 apply 함으로써 recyclerview + viewpager 사용
            binding.mediaViewpager.apply {
                adapter = stepMediaRecyclerviewAdapter
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
            }
        }
    }

    // step data에서 이미지, 영상 정보만 추출
    private fun makeMediaUriList() {
        for(item in datas)   {
            val tempList = mutableListOf<StepMediaData>()

            for (data in item.imageData) {
                tempList.add(StepMediaData(data.toUri(), "image"))
            }
            if (item.videoData != null) {
                for (data in item.videoData) {
                    tempList.add(StepMediaData(data.toUri(), "video"))
                }
            }

            mediaUris.add(tempList)
        }
    }
}