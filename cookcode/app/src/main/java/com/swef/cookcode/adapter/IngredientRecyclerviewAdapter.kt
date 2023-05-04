package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.IngredientData
import com.swef.cookcode.databinding.IngredientRecyclerviewItemBinding

class IngredientRecyclerviewAdapter()
    :RecyclerView.Adapter<IngredientRecyclerviewAdapter.ViewHolder>() {

    var datas = mutableListOf<IngredientData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IngredientRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(
        private val binding: IngredientRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IngredientData){

        }
    }
}