package com.swef.cookcode.navifrags

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.IngredientRecyclerviewAdapter
import com.swef.cookcode.data.host.IngredientDataHost
import com.swef.cookcode.adapter.RefrigeratorRecyclerAdapter
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.RefrigeratorData
import com.swef.cookcode.databinding.FragmentRefrigeratorBinding
import com.swef.cookcode.databinding.RefrigeratorIngredientSelectDialogBinding

class RefrigeratorFragment : Fragment() {

    private var _binding: FragmentRefrigeratorBinding? = null
    private val binding get() = _binding!!

    private lateinit var refrigeratorRecyclerAdapter: RefrigeratorRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefrigeratorBinding.inflate(inflater, container, false)

        refrigeratorRecyclerAdapter = RefrigeratorRecyclerAdapter()
        binding.recyclerView.adapter = refrigeratorRecyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        refrigeratorRecyclerAdapter.datas.apply {
            add(RefrigeratorData("육류", "meat"))
            add(RefrigeratorData("해산물", "seafood"))
            add(RefrigeratorData("유제품", "diary_product"))
            add(RefrigeratorData("곡물", "grain"))
            add(RefrigeratorData("채소", "vegetable"))
            add(RefrigeratorData("과일", "fruit"))
            add(RefrigeratorData("양념", "sauce"))
        }

        // 임시 Data
        val ingredDatas = mutableListOf<MyIngredientData>()
        ingredDatas.apply {
            add(IngredientDataHost().getIngredientFromId(40)!!)
            add(IngredientDataHost().getIngredientFromId(1)!!)
            add(IngredientDataHost().getIngredientFromId(3)!!)
            add(IngredientDataHost().getIngredientFromId(25)!!)
            add(IngredientDataHost().getIngredientFromId(55)!!)
            add(IngredientDataHost().getIngredientFromId(13)!!)
            add(IngredientDataHost().getIngredientFromId(49)!!)
        }
        ingredDatas[0].value = 500
        ingredDatas[1].value = 500
        ingredDatas[2].value = 500
        ingredDatas[3].value = 500
        ingredDatas[4].value = 500
        ingredDatas[5].value = 500
        ingredDatas[6].value = 500
        refrigeratorRecyclerAdapter.ingredDatas = ingredDatas
        refrigeratorRecyclerAdapter.notifyDataSetChanged()

        // 식재료 등록 Dialog
        val refrigeratorDialogView = RefrigeratorIngredientSelectDialogBinding.inflate(layoutInflater)
        val refrigeratorAlertDialog = AlertDialog.Builder(context)
            .setView(refrigeratorDialogView.root)
            .create()

        val onDialogRecyclerViewItemClickListener = object : OnDialogRecyclerviewItemClickListener {
            override fun onItemClicked() {
                // 2. 기존 Dialog 숨기기 및 새 Dialog 생성
                refrigeratorAlertDialog.dismiss()
            }
        }

        val ingredientData = IngredientDataHost().showAllIngredientData()
        val searchIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("refrigerator_search", onDialogRecyclerViewItemClickListener)

        searchIngredientRecyclerviewAdapter.filteredDatas = ingredientData as MutableList<MyIngredientData>
        refrigeratorDialogView.recyclerView.adapter = searchIngredientRecyclerviewAdapter
        refrigeratorDialogView.recyclerView.layoutManager = GridLayoutManager(context, 3)

        val addTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchIngredientRecyclerviewAdapter.filteredDatas = IngredientDataHost().getIngredientFromNameOrType(
                    searchIngredientRecyclerviewAdapter.beforeSearchData, p0.toString()) as MutableList<MyIngredientData>
                searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {}
        }
        refrigeratorDialogView.ingredientName.addTextChangedListener(addTextChangedListener)

        refrigeratorDialogView.btnCancel.setOnClickListener {
            refrigeratorAlertDialog.dismiss()
        }

        // 식재료 등록 버튼
        binding.addIngredient.setOnClickListener {
            searchIngredientRecyclerviewAdapter.datas = ingredientData
            searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
            refrigeratorAlertDialog.show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface OnDialogRecyclerviewItemClickListener {
    fun onItemClicked()
}