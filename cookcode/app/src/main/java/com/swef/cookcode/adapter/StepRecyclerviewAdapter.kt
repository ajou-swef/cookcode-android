package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.StepRecyclerviewItemBinding
import com.swef.cookcode.`interface`.StepOnClickListener

class StepRecyclerviewAdapter(
    private val listener: StepOnClickListener
): RecyclerView.Adapter<StepRecyclerviewAdapter.ViewHolder>(){

    var datas = mutableListOf<StepData>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StepRecyclerviewAdapter.ViewHolder {
        val binding = StepRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepRecyclerviewAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: StepRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StepData){
            binding.stepText.text = item.numberOfStep.toString() + "단계"
            binding.stepText.setOnClickListener {
                listener.stepOnClick(item.numberOfStep)
            }
        }

    }
}