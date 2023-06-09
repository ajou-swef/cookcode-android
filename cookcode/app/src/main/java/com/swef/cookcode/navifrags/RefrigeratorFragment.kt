package com.swef.cookcode.navifrags

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.R
import com.swef.cookcode.adapter.IngredientRecyclerviewAdapter
import com.swef.cookcode.adapter.RefrigeratorRecyclerAdapter
import com.swef.cookcode.data.GlobalVariables.fridgeAPI
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.RefrigeratorData
import com.swef.cookcode.data.host.IngredientDataHost
import com.swef.cookcode.data.response.FridgeResponse
import com.swef.cookcode.data.response.MyIngredList
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.FragmentRefrigeratorBinding
import com.swef.cookcode.databinding.RefrigeratorIngredientSelectDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefrigeratorFragment : Fragment() {

    private var _binding: FragmentRefrigeratorBinding? = null
    private val binding get() = _binding!!

    private lateinit var refrigeratorRecyclerAdapter: RefrigeratorRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefrigeratorBinding.inflate(inflater, container, false)

        val ingredDatas = mutableListOf<MyIngredientData>()

        // 식재료 등록 Dialog
        val refrigeratorDialogView = RefrigeratorIngredientSelectDialogBinding.inflate(layoutInflater)
        val refrigeratorAlertDialog = AlertDialog.Builder(context)
            .setView(refrigeratorDialogView.root)
            .create()

        val onDialogRecyclerViewItemClickListener = object : OnDialogRecyclerviewItemClickListener {
            override fun onItemClicked() {
                // 기존 Dialog 숨기기 및 새 Dialog 생성
                refrigeratorAlertDialog.dismiss()
            }

            override suspend fun postIngredient(ingredId: Int, expiredAt: String, quantity: Int): Int{
                val postData = HashMap<String, Any>()
                postData["ingredId"] = ingredId
                postData["expiredAt"] = expiredAt
                postData["quantity"] = quantity

                var fridgeId = 0

                Thread {
                    fridgeAPI.postIngredientData(postData)
                        .enqueue(object : Callback<FridgeResponse> {
                            override fun onResponse(
                                call: Call<FridgeResponse>,
                                response: Response<FridgeResponse>
                            ) {
                                if (response.body() != null) {
                                    if (response.body()!!.status == 200) {
                                        fridgeId = response.body()!!.data.fridgeIngredId
                                        Toast.makeText(
                                            context,
                                            "식재료 등록이 완료되었습니다.",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                } else {
                                    Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<FridgeResponse>, t: Throwable) {
                                Toast.makeText(context, R.string.err_server, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                }.start()

                try {
                    withContext(Dispatchers.IO) {
                        Thread.sleep(100)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return fridgeId
            }

            override fun deleteIngredient(fridgeId: Int) {
                fridgeAPI.deleteIngredientData(fridgeId).enqueue(object: Callback<StatusResponse>{
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if(response.body() != null) {
                            if (response.body()!!.status == 200) {
                                Toast.makeText(context, "식재료 삭제가 완료되었습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        else {
                            Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        Toast.makeText(context, R.string.err_server, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun patchIngredient(fridgeId: Int, ingredId: Int, expiredAt: String, quantity: Int) {
                val patchData = HashMap<String, Any>()
                patchData["ingredId"] = ingredId
                patchData["expiredAt"] = expiredAt
                patchData["quantity"] = quantity
                fridgeAPI.patchIngredientData(fridgeId, patchData).enqueue(object: Callback<StatusResponse>{
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if(response.body() != null) {
                            if (response.body()!!.status == 200) {
                                Toast.makeText(context, "식재료 수정이 완료되었습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        else {
                            Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        Toast.makeText(context, R.string.err_server, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun getIngredient() {
                val ingredientDatas = mutableListOf<MyIngredientData>()

                fridgeAPI.getFridgeData().enqueue(object: Callback<MyIngredList> {
                    override fun onResponse(call: Call<MyIngredList>, response: Response<MyIngredList>){
                        if(response.body() != null) {
                            val myIngredientData = response.body()!!.data.ingreds
                            for (item in myIngredientData) {
                                ingredientDatas.apply {
                                    val ingredData = IngredientDataHost().getIngredientFromId(item.IngredId)!!.ingredientData
                                    val fridgeId = item.fridgeIngredId
                                    val value = item.value
                                    val expiredAt = item.expiredAt
                                    add(MyIngredientData(ingredData, fridgeId, value, expiredAt, null))
                                }
                            }

                            refrigeratorRecyclerAdapter.ingredDatas = ingredientDatas
                            refrigeratorRecyclerAdapter.notifyItemRangeChanged(0, ingredDatas.size)
                        }
                    }

                    override fun onFailure(call: Call<MyIngredList>, t: Throwable) {
                        Toast.makeText(context, R.string.err_server, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun updateExpandRecyclerview(data: MyIngredientData) {
                refrigeratorRecyclerAdapter.updateData(data)
            }
        }

        refrigeratorRecyclerAdapter = RefrigeratorRecyclerAdapter(onDialogRecyclerViewItemClickListener)
        binding.recyclerView.adapter = refrigeratorRecyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        val refridgeData = mutableListOf<RefrigeratorData>().apply {
            add(RefrigeratorData("육류", "meat"))
            add(RefrigeratorData("해산물", "seafood"))
            add(RefrigeratorData("유제품", "diary_product"))
            add(RefrigeratorData("곡물", "grain"))
            add(RefrigeratorData("채소", "vegetable"))
            add(RefrigeratorData("과일", "fruit"))
            add(RefrigeratorData("양념", "sauce"))
        }
        refrigeratorRecyclerAdapter.datas = refridgeData

        val ingredientData = IngredientDataHost().showAllIngredientData()
        val searchIngredientRecyclerviewAdapter = IngredientRecyclerviewAdapter("refrigerator_search", onDialogRecyclerViewItemClickListener)

        fridgeAPI.getFridgeData().enqueue(object : Callback<MyIngredList> {
            override fun onResponse(
                call: Call<MyIngredList>,
                response: Response<MyIngredList>
            ) {
                if (response.body() != null) {
                    val myIngredientData = response.body()!!.data.ingreds
                    for (item in myIngredientData) {
                        ingredDatas.apply {
                            val ingredData =
                                IngredientDataHost().getIngredientFromId(item.IngredId)!!.ingredientData
                            val fridgeId = item.fridgeIngredId
                            val value = item.value
                            val expiredAt = item.expiredAt
                            add(MyIngredientData(ingredData, fridgeId, value, expiredAt, null))
                        }
                    }
                    refrigeratorRecyclerAdapter.ingredDatas = ingredDatas
                    refrigeratorRecyclerAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<MyIngredList>, t: Throwable) {
                Toast.makeText(context, R.string.err_server, Toast.LENGTH_SHORT).show()
            }
        })

        searchIngredientRecyclerviewAdapter.filteredDatas = ingredientData as MutableList<MyIngredientData>
        searchIngredientRecyclerviewAdapter.beforeSearchData = ingredientData
        refrigeratorDialogView.recyclerView.adapter = searchIngredientRecyclerviewAdapter
        refrigeratorDialogView.recyclerView.layoutManager = GridLayoutManager(context, 3)

        val addTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(beforeText: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(currentText: CharSequence?, start: Int, before: Int, count: Int) {
                searchIngredientRecyclerviewAdapter.filteredDatas = IngredientDataHost().getIngredientFromNameOrType(
                    searchIngredientRecyclerviewAdapter.beforeSearchData, currentText.toString()) as MutableList<MyIngredientData>
                searchIngredientRecyclerviewAdapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(afterText: Editable?) {}
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
    suspend fun postIngredient(ingredId: Int, expiredAt: String, quantity: Int): Int
    fun patchIngredient(fridgeId: Int, ingredId: Int, expiredAt: String, quantity: Int)
    fun deleteIngredient(fridgeId: Int)
    fun getIngredient()
    fun updateExpandRecyclerview(data: MyIngredientData)
}