package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.RefrigeratorData
import com.swef.cookcode.data.host.ToggleAnimationHost
import com.swef.cookcode.databinding.RefrigeratorRecyclerviewItemBinding
import com.swef.cookcode.navifrags.OnDialogRecyclerviewItemClickListener

class RefrigeratorRecyclerAdapter(
    private val listener: OnDialogRecyclerviewItemClickListener
)
    : RecyclerView.Adapter<RefrigeratorRecyclerAdapter.ViewHolder>() {

    var datas = mutableListOf<RefrigeratorData>()
    var ingredDatas = mutableListOf<MyIngredientData>()
    private val ingredientRecyclerviewAdapters = mutableListOf<IngredientRecyclerviewAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RefrigeratorRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        for (i: Int in 0..6){
            ingredientRecyclerviewAdapters.add(IngredientRecyclerviewAdapter("refrigerator", listener))
        }
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(
        private val binding: RefrigeratorRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RefrigeratorData){
            binding.ingredientName.text = item.type

            // val ingredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("refrigerator", listener)

            binding.recyclerView.adapter = ingredientRecyclerviewAdapters[position]
            binding.recyclerView.layoutManager = GridLayoutManager(
                binding.recyclerView.context, 3)

            ingredientRecyclerviewAdapters[position].filteredDatas =
                ingredDatas.filter { it.ingredientData.type == item.type_en } as MutableList<MyIngredientData>

            ingredientRecyclerviewAdapters[position].notifyDataSetChanged()

            // 화살표를 누르면 레이아웃이 확장된다
            binding.btnMore.setOnClickListener {
                val show = toggleLayout(!item.isExpanded, it, binding.layoutExpand)
                item.isExpanded = show
            }
        }

        // 레이아웃 확장 함수
        // host package의 ToggleAnimationHost class를 통해 확장 구현
        private fun toggleLayout(isExpanded: Boolean, view: View, layoutExpand: LinearLayout): Boolean {
            ToggleAnimationHost.toggleArrow(view, isExpanded)
            if (isExpanded) {
                ToggleAnimationHost.expand(layoutExpand)
            } else {
                ToggleAnimationHost.collapse(layoutExpand)
            }
            return isExpanded
        }
    }

    fun updateData(data: MyIngredientData) {
        ingredDatas.add(data)
        val position = datas.indexOf(datas.find { it.type_en == data.ingredientData.type })
        ingredientRecyclerviewAdapters[position].filteredDatas.add(data)
        ingredientRecyclerviewAdapters[position].notifyItemInserted(ingredientRecyclerviewAdapters[position].filteredDatas.size - 1)
    }
}