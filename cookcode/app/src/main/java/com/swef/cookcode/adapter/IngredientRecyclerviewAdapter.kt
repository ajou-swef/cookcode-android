package com.swef.cookcode.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.databinding.IngredientRecyclerviewItemBinding
import com.swef.cookcode.databinding.RecipeIngredientDialogBinding
import com.swef.cookcode.databinding.RefrigeratorIngredientDialogBinding

class IngredientRecyclerviewAdapter(
    private val type: String
)
    :RecyclerView.Adapter<IngredientRecyclerviewAdapter.ViewHolder>() {

    var datas = mutableListOf<MyIngredientData>()
    var filteredDatas = mutableListOf<MyIngredientData>()
    var selectedItems = mutableListOf<MyIngredientData>()
    var essentialData = mutableListOf<MyIngredientData>()
    var additionalData = mutableListOf<MyIngredientData>()
    var beforeSearchData = mutableListOf<MyIngredientData>()
    lateinit var selectedItem: MyIngredientData


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IngredientRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, parent)
    }

    override fun getItemCount(): Int = filteredDatas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredDatas[position])
    }

    inner class ViewHolder(
        private val binding: IngredientRecyclerviewItemBinding,
        private val parent: ViewGroup
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyIngredientData) {
            binding.ingredientIcon.setImageURI(item.ingredientData.image)
            binding.ingredientName.text = item.ingredientData.name

            // 해당 어댑터를 사용하는 경우는 총 3가지
            // 식재료 등록용 어댑터
            when (type) {
                "search" -> {
                    if (item.visibility == null) {
                        item.visibility = View.GONE
                    }
                    binding.value.visibility = View.GONE
                    binding.checked.visibility = item.visibility!!
                    // 식재료가 선택되었을 경우 선택된 아이템을 저장해둠
                    // 해제할 경우 삭제
                    binding.layout.setOnClickListener {
                        if (item.visibility == View.GONE) {
                            item.visibility = View.VISIBLE
                            selectedItems.apply {
                                if (selectedItems.find { it.ingredientData.name == item.ingredientData.name } == null)
                                    add(item)
                            }
                        } else {
                            item.visibility = View.GONE
                            selectedItems.remove(item)
                        }
                        binding.checked.visibility = item.visibility!!
                    }
                }
                // 레시피에 등록된 식재료 수정용 어댑터
                "recipe" -> {
                    binding.value.visibility = View.VISIBLE
                    binding.value.text = item.value.toString()

                    // 식재료 양 조절
                    binding.layout.setOnClickListener {
                        val recipeDialogView = RecipeIngredientDialogBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                        val recipeAlertDialog = AlertDialog.Builder(parent.context)
                            .setView(recipeDialogView.root)
                            .create()

                        recipeDialogView.btnCancel.setOnClickListener {
                            recipeAlertDialog.dismiss()
                        }
                        recipeDialogView.btnConfirm.setOnClickListener {
                            binding.value.text = recipeDialogView.ingredientValue.text
                            // 서버에 변경 요청
                            recipeAlertDialog.dismiss()
                        }
                    }
                }
                // 냉장고에 등록된 식재료 수정용 어댑터
                "refrigerator" -> {
                    binding.value.visibility = View.VISIBLE
                    binding.value.text = item.value.toString()

                    // 식재료 양, 유통기한 조정
                    binding.layout.setOnClickListener {
                        val refrigeratorDialogView = RefrigeratorIngredientDialogBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                        val refrigeratorAlertDialog = AlertDialog.Builder(parent.context)
                            .setView(refrigeratorDialogView.root)
                            .create()

                        refrigeratorDialogView.btnCancel.setOnClickListener {
                            refrigeratorAlertDialog.dismiss()
                        }
                        refrigeratorDialogView.btnConfirm.setOnClickListener {
                            binding.value.text = refrigeratorDialogView.ingredientValue.text
                            // 서버에 변경 요청
                            refrigeratorAlertDialog.dismiss()
                        }
                    }
                }
            }
        }
    }
}