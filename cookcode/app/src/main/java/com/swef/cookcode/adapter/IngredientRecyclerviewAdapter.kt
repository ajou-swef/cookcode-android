package com.swef.cookcode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.databinding.IngredientRecyclerviewItemBinding

class IngredientRecyclerviewAdapter(
    private val type: String
)
    :RecyclerView.Adapter<IngredientRecyclerviewAdapter.ViewHolder>() {

    var datas = mutableListOf<MyIngredientData>()
    var selectedItems = mutableListOf<MyIngredientData>()
    lateinit var selectedItem: MyIngredientData

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
        fun bind(item: MyIngredientData){
            binding.ingredientIcon.setImageURI(item.ingredientData.image)
            binding.ingredientName.text = item.ingredientData.name

            // 해당 어댑터를 사용하는 경우는 총 3가지
            // 식재료 등록용 어댑터
            if (type == "search") {
                binding.value.visibility = View.GONE
                // 식재료가 선택되었을 경우 선택된 아이템을 저장해둠
                // 해제할 경우 삭제
                binding.layout.setOnClickListener {
                    if (binding.checked.visibility == View.GONE) {
                        binding.checked.visibility = View.VISIBLE
                        selectedItems.add(item)
                    }
                    else {
                        binding.checked.visibility = View.GONE
                        selectedItems.remove(item)
                    }
                }
            }
            // 레시피에 등록된 식재료 수정용 어댑터
            else if (type == "recipe") {
                binding.value.visibility = View.VISIBLE
                binding.value.text = item.value.toString()
                // 식재료 양 조절
                binding.layout.setOnClickListener {

                }
            }
            // 냉장고에 등록된 식재료 수정용 어댑터
            else if (type == "fridge") {
                binding.value.visibility = View.VISIBLE
                binding.value.text = item.value.toString()
                // 식재료 양, 유통기한 조정
                binding.layout.setOnClickListener {

                }
            }
        }
    }
}