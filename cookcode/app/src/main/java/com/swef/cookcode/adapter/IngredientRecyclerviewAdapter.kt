package com.swef.cookcode.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.GlobalVariables.ERR_CODE
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.databinding.IngredientRecyclerviewItemBinding
import com.swef.cookcode.databinding.RecipeIngredientDialogBinding
import com.swef.cookcode.databinding.RefrigeratorIngredientDialogBinding
import com.swef.cookcode.navifrags.OnDialogRecyclerviewItemClickListener
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class IngredientRecyclerviewAdapter(
    private val type: String
)
    :RecyclerView.Adapter<IngredientRecyclerviewAdapter.ViewHolder>() {
    constructor(type: String, listener: OnDialogRecyclerviewItemClickListener) : this(type) {
        this.listener = listener
    }

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

    private lateinit var listener: OnDialogRecyclerviewItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IngredientRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, parent)
    }

    override fun getItemCount(): Int = filteredDatas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredDatas[position], position)
    }

    inner class ViewHolder(
        private val binding: IngredientRecyclerviewItemBinding,
        private val parent: ViewGroup
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyIngredientData, position: Int) {
            binding.ingredientIcon.setImageURI(item.ingredientData.image)
            binding.ingredientName.text = item.ingredientData.name

            // 해당 어댑터를 사용하는 경우는 총 3가지
            // 식재료 등록용 어댑터
            when (type) {
                "recipe_search" -> {
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
                            item.value = null
                            selectedItems.remove(item)
                        }
                        binding.checked.visibility = item.visibility!!
                    }
                }
                "recipe_preview" -> {
                    val coopangUrl = "https://www.coupang.com/np/search?q="

                    binding.value.visibility = View.GONE
                    if (item.isLack!!) {
                        binding.lack.visibility = View.VISIBLE
                        binding.layout.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${coopangUrl}${item.ingredientData.name}"))
                            parent.context.startActivity(intent)
                        }
                    }
                    else {
                        binding.lack.visibility = View.GONE
                    }
                }
                // 레시피에 등록된 식재료 수정용 어댑터
                "recipe" -> {
                    binding.value.visibility = View.VISIBLE
                    if (item.value == null) {
                        binding.value.setTextColor(getColorFromContext("red", parent))
                        binding.value.text = "입력 필요"
                    } else {
                        binding.value.setTextColor(getColorFromContext("black", parent))
                        binding.value.text = parent.context.getString(
                            R.string.ingred_quantity, item.value, item.ingredientData.unit)
                    }

                    val recipeDialogView = RecipeIngredientDialogBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)

                    val recipeAlertDialog = AlertDialog.Builder(parent.context)
                        .setView(recipeDialogView.root)
                        .create()

                    recipeDialogView.ingredientValue.text = parent.context.getString(
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
                            binding.value.setTextColor(getColorFromContext("black", parent))
                            recipeAlertDialog.dismiss()
                        }
                    }

                    recipeDialogView.editIngredientValue.onFocusChangeListener = View.OnFocusChangeListener {
                        view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, parent)
                    }

                    // 식재료 양 조절
                    binding.layout.setOnClickListener {
                        recipeAlertDialog.show()
                    }
                }
                "refrigerator_search" -> {
                    binding.value.visibility = View.GONE

                    val refrigeratorDialogView = RefrigeratorIngredientDialogBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)

                    val refrigeratorAlertDialog = AlertDialog.Builder(parent.context)
                        .setView(refrigeratorDialogView.root)
                        .create()

                    refrigeratorDialogView.ingredientValue.text = parent.context.getString(
                            R.string.ingredient_value,
                            item.ingredientData.unit
                        )

                    refrigeratorDialogView.editIngredientName.setText(item.ingredientData.name)

                    refrigeratorDialogView.btnDelete.text = "취소"
                    refrigeratorDialogView.btnDelete.setOnClickListener {
                        refrigeratorAlertDialog.dismiss()
                    }

                    refrigeratorDialogView.btnConfirm.bringToFront()

                    refrigeratorDialogView.btnConfirm.setOnClickListener {
                        val value = refrigeratorDialogView.editIngredientValue.text.toString()
                        val date = refrigeratorDialogView.editIngredientExpiredAt.text.toString()

                        if (testValueOrDateIsValid(value, date, parent))  {
                            item.expiredAt = date
                            item.value = Integer.parseInt(value)

                            binding.value.text = parent.context.getString(
                                R.string.ingred_quantity,
                                item.value, item.ingredientData.unit
                            )

                            CoroutineScope(Dispatchers.Default).launch {
                                item.fridgeIngredId = listener.postIngredient(
                                    item.ingredientData.ingredId,
                                    item.expiredAt!!,
                                    item.value!!
                                )
                            }

                            listener.updateExpandRecyclerview(item)
                            refrigeratorAlertDialog.dismiss()
                        }
                    }

                    refrigeratorDialogView.editIngredientValue.onFocusChangeListener = View.OnFocusChangeListener {
                            view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, parent)
                    }

                    refrigeratorDialogView.editIngredientExpiredAt.onFocusChangeListener = View.OnFocusChangeListener {
                            view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, parent)
                    }

                    refrigeratorDialogView.editIngredientExpiredAt.addTextChangedListener(dateTypeTextAutomaticallyChange(refrigeratorDialogView))

                    binding.layout.setOnClickListener {
                        listener.onItemClicked()
                        refrigeratorAlertDialog.show()
                    }
                }
                // 냉장고에 등록된 식재료 수정용 어댑터
                "refrigerator" -> {
                    binding.value.visibility = View.VISIBLE
                    binding.value.setTextColor(getColorFromContext("black", parent))
                    binding.value.text = parent.context.getString(
                        R.string.ingred_quantity, item.value, item.ingredientData.unit)

                    val currentDate = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val ingredientExpiredDate = LocalDate.parse(item.expiredAt, formatter)

                    val isExpiredIngredient = ingredientExpiredDate.minusDays(7)

                    if (isExpiredIngredient.isBefore(currentDate)) {
                        binding.expiredSoon.visibility = View.VISIBLE
                    }
                    else {
                        binding.expiredSoon.visibility = View.GONE
                    }

                    val refrigeratorDialogView = RefrigeratorIngredientDialogBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)

                    val refrigeratorAlertDialog = AlertDialog.Builder(parent.context)
                        .setView(refrigeratorDialogView.root)
                        .create()

                    refrigeratorDialogView.ingredientValue.text = parent.context.getString(
                            R.string.ingredient_value,
                            item.ingredientData.unit
                        )

                    if (item.value != null)
                        refrigeratorDialogView.editIngredientValue.setText(item.value.toString())

                    if (item.expiredAt != null)
                        refrigeratorDialogView.editIngredientExpiredAt.setText(item.expiredAt.toString())

                    refrigeratorDialogView.editIngredientName.setText(item.ingredientData.name)

                    refrigeratorDialogView.btnDelete.setOnClickListener {
                        listener.deleteIngredient(item.fridgeIngredId!!)
                        filteredDatas.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeRemoved(position, filteredDatas.size - position)
                        refrigeratorAlertDialog.dismiss()
                    }

                    refrigeratorDialogView.btnConfirm.bringToFront()

                    refrigeratorDialogView.btnConfirm.setOnClickListener {
                        val value = refrigeratorDialogView.editIngredientValue.text.toString()
                        val date = refrigeratorDialogView.editIngredientExpiredAt.text.toString()

                        if (testValueOrDateIsValid(value, date, parent)) {
                            item.expiredAt = date
                            item.value = Integer.parseInt(value)

                            binding.value.text = parent.context.getString(
                                R.string.ingred_quantity,
                                item.value, item.ingredientData.unit
                            )

                            listener.patchIngredient(item.fridgeIngredId!!,
                                item.ingredientData.ingredId,
                                item.expiredAt!!,
                                item.value!!)

                            notifyItemChanged(position)
                            refrigeratorAlertDialog.dismiss()
                        }
                    }

                    refrigeratorDialogView.editIngredientValue.onFocusChangeListener = View.OnFocusChangeListener {
                            view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, parent)
                    }

                    refrigeratorDialogView.editIngredientExpiredAt.onFocusChangeListener = View.OnFocusChangeListener {
                            view, hasFocus -> hideKeyboardFromEditText(view, hasFocus, parent)
                    }

                    refrigeratorDialogView.editIngredientExpiredAt.addTextChangedListener(dateTypeTextAutomaticallyChange(refrigeratorDialogView))


                    // 식재료 양, 유통기한 조정
                    binding.layout.setOnClickListener {
                        refrigeratorAlertDialog.show()
                    }
                }
            }
        }
    }

    private fun hideKeyboardFromEditText(view: View, hasFocus: Boolean, parent: ViewGroup) {
        val hideFlags = 0
        if (!hasFocus) {
            val inputMethodManager = parent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, hideFlags)
        }
    }

    private fun testValueOrDateIsValid(value: String, date: String, parent: ViewGroup): Boolean {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedCurrentDate = LocalDate.parse(currentDate.toString(), formatter)
        val ingredientExpiredDate = LocalDate.parse(date, formatter)

        if (value.isEmpty()) {
            Toast.makeText(parent.context, "양이 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
        else if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}$"))) {
            Toast.makeText(parent.context, "정확한 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
        else if (!date.matches(Regex("20\\d{2}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])"))){
            Toast.makeText(parent.context, "정확한 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
        else if (ingredientExpiredDate.isBefore(formattedCurrentDate)){
            Toast.makeText(parent.context, "오늘 이전의 날짜는 입력할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
        else
            return true

        return false
    }

    private fun getColorFromContext(color: String, parent: ViewGroup): Int {
        return if (color == "black")
            ContextCompat.getColor(parent.context, R.color.black)
        else if(color == "red")
            ContextCompat.getColor(parent.context, R.color.red)
        else ERR_CODE
    }

    private fun dateTypeTextAutomaticallyChange(refrigeratorDialogView: RefrigeratorIngredientDialogBinding): TextWatcher {
        return object: TextWatcher {
            var beforeText = ""

            override fun beforeTextChanged(beforeText: CharSequence?, start: Int, count: Int, after: Int) {
                this.beforeText = beforeText.toString()
            }

            override fun onTextChanged(currentText: CharSequence?, start: Int, before: Int, count: Int) {
                // Do Nothing
            }

            override fun afterTextChanged(afterText: Editable?) {
                // Do Nothing
                val input = afterText?.toString() ?: ""

                val formattedInput = if (input.length == 4 || input.length == 7) {
                    if (beforeText.last() == '-') {
                        input.substring(0, input.length - 1)
                    } else {
                        "$input-"
                    }
                } else {
                    input
                }

                if (input != formattedInput) {
                    refrigeratorDialogView.editIngredientExpiredAt.setText(formattedInput)
                    refrigeratorDialogView.editIngredientExpiredAt.setSelection(formattedInput.length)
                }
            }
        }
    }
}