package com.swef.cookcode.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.databinding.IngredientRecyclerviewItemBinding
import com.swef.cookcode.databinding.RecipeIngredientDialogBinding
import com.swef.cookcode.databinding.RefrigeratorIngredientDialogBinding
import java.text.SimpleDateFormat
import java.util.Locale

class IngredientRecyclerviewAdapter(
    private val type: String
)
    :RecyclerView.Adapter<IngredientRecyclerviewAdapter.ViewHolder>() {

    // datas는 데이터베이스에 들어있는 식재료 원본
    // 필터 검색을 위해서 filteredDatas에 검색 결과를 저장해서 binding
    var datas = mutableListOf<MyIngredientData>()
    var filteredDatas = mutableListOf<MyIngredientData>()

    // recipe에 식재료 등록 시 선택된 식재료들을 담아둠
    var selectedItems = mutableListOf<MyIngredientData>()

    // 필수 재료, 추가 재료 정보를 담아둠
    var essentialData = mutableListOf<MyIngredientData>()
    var additionalData = mutableListOf<MyIngredientData>()

    // 필터 검색 시 필수재료, 추가재료 별로 검색하는 데이터 원본이 다르므로 담아두고 검색 결과를 filteredDatas로 넘겨줌
    var beforeSearchData = mutableListOf<MyIngredientData>()

    // 냉장고에 식재료 등록 시 선택된 아이템
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
                    if (item.value == null) {
                        binding.value.setTextColor(
                            ContextCompat.getColor(
                                parent.context,
                                R.color.red
                            )
                        )
                        binding.value.text = "입력 필요"
                    } else {
                        binding.value.setTextColor(
                            ContextCompat.getColor(
                                parent.context,
                                R.color.black
                            )
                        )
                        binding.value.text = parent.context.getString(
                            R.string.ingred_quantity, item.value, item.ingredientData.unit)
                    }

                    val recipeDialogView = RecipeIngredientDialogBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    val recipeAlertDialog = AlertDialog.Builder(parent.context)
                        .setView(recipeDialogView.root)
                        .create()

                    recipeDialogView.ingredientValue.text =
                        parent.context.getString(
                            R.string.ingredient_value,
                            item.ingredientData.unit
                        )

                    recipeDialogView.editIngredientName.setText(item.ingredientData.name)

                    recipeDialogView.btnCancel.setOnClickListener {
                        recipeAlertDialog.dismiss()
                    }
                    recipeDialogView.btnConfirm.setOnClickListener {
                        if (recipeDialogView.editIngredientValue.text.isNullOrEmpty()) {
                            Toast.makeText(parent.context, "양이 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            item.value =
                                Integer.parseInt(recipeDialogView.editIngredientValue.text.toString())
                            binding.value.text = parent.context.getString(
                                R.string.ingred_quantity,
                                item.value, item.ingredientData.unit
                            )
                            // 서버에 변경 요청
                            recipeAlertDialog.dismiss()
                        }
                    }

                    recipeDialogView.root.setOnClickListener { v ->
                        if (v !is EditText) {
                            val imm =
                                parent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                        }
                        v.clearFocus()
                    }

                    // 식재료 양 조절
                    binding.layout.setOnClickListener {
                        recipeAlertDialog.show()
                    }
                }
                // 냉장고에 등록된 식재료 수정용 어댑터
                "refrigerator" -> {
                    binding.value.visibility = View.VISIBLE
                    binding.value.text = parent.context.getString(
                        R.string.ingred_quantity, item.value, item.ingredientData.unit)

                    val refrigeratorDialogView = RefrigeratorIngredientDialogBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    val refrigeratorAlertDialog = AlertDialog.Builder(parent.context)
                        .setView(refrigeratorDialogView.root)
                        .create()
                    refrigeratorDialogView.ingredientValue.text =
                        parent.context.getString(
                            R.string.ingredient_value,
                            item.ingredientData.unit
                        )

                    refrigeratorDialogView.editIngredientName.setText(item.ingredientData.name)

                    refrigeratorDialogView.btnCancel.setOnClickListener {
                        refrigeratorAlertDialog.dismiss()
                    }
                    refrigeratorDialogView.btnConfirm.setOnClickListener {
                        val dateString = refrigeratorDialogView.editIngredientName.text.toString()
                        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                        val date = dateFormat.parse(dateString)
                        item.expiredAt = date
                        item.value = Integer.parseInt(refrigeratorDialogView.editIngredientValue.text.toString())

                        binding.value.text = parent.context.getString(
                            R.string.ingred_quantity,
                            item.value, item.ingredientData.unit)
                        // 서버에 변경 요청
                        refrigeratorAlertDialog.dismiss()
                    }

                    refrigeratorDialogView.editIngredientValue.onFocusChangeListener = View.OnFocusChangeListener {
                            v, hasFocus ->
                        if (!hasFocus) {
                            val imm =
                                parent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                        }
                    }

                    refrigeratorDialogView.editIngredientExpiredAt.onFocusChangeListener = View.OnFocusChangeListener {
                            v, hasFocus ->
                        if (!hasFocus) {
                            val imm =
                                parent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                        }
                    }

                    // 식재료 양, 유통기한 조정
                    binding.layout.setOnClickListener {
                        refrigeratorAlertDialog.show()
                    }
                }
            }
        }
    }
}