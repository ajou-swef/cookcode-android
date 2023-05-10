package com.swef.cookcode.navifrags

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.R
import com.swef.cookcode.adapter.IngredientRecyclerviewAdapter
import com.swef.cookcode.data.host.IngredientDataHost
import com.swef.cookcode.adapter.RefrigeratorRecyclerAdapter
import com.swef.cookcode.api.FridgeAPI
import com.swef.cookcode.data.MyIngredientData
import com.swef.cookcode.data.RefrigeratorData
import com.swef.cookcode.data.response.MyIngredList
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.FragmentRefrigeratorBinding
import com.swef.cookcode.databinding.RefrigeratorIngredientSelectDialogBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefrigeratorFragment : Fragment() {

    private var _binding: FragmentRefrigeratorBinding? = null
    private val binding get() = _binding!!

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private val API = FridgeAPI.create()

    private lateinit var refrigeratorRecyclerAdapter: RefrigeratorRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefrigeratorBinding.inflate(inflater, container, false)

        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!

        val ingredDatas = mutableListOf<MyIngredientData>()

        API.getFridgeData(accessToken).enqueue(object: Callback<MyIngredList> {
            override fun onResponse(call: Call<MyIngredList>, response: Response<MyIngredList>){
                if(response.body() != null) {
                    val myIngredientData = response.body()!!.data.ingreds
                    for (item in myIngredientData) {
                        ingredDatas.apply {
                            val ingredData = IngredientDataHost().getIngredientFromId(item.IngredId)!!.ingredientData
                            val fridgeId = item.fridgeIngredId
                            val value = item.value
                            val expiredAt = item.expiredAt
                            add(MyIngredientData(ingredData, fridgeId, value, expiredAt, null))
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MyIngredList>, t: Throwable) {
                Toast.makeText(context, R.string.err_server, Toast.LENGTH_SHORT).show()
            }
        })

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

            override fun postIngredient(ingredId: Int, expiredAt: String, quantity: Int) {
                val postData = HashMap<String, Any>()
                postData["ingredId"] = ingredId
                postData["expiredAt"] = expiredAt
                postData["quantity"] = quantity
                API.postIngredientData(accessToken, postData).enqueue(object: Callback<StatusResponse> {
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if(response.body() != null) {
                            if (response.body()!!.status == 200) {
                                Toast.makeText(context, "식재료 등록이 완료되었습니다.", Toast.LENGTH_SHORT)
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

            override fun deleteIngredient(fridgeId: Int) {
                API.deleteIngredientData(accessToken, fridgeId).enqueue(object: Callback<StatusResponse>{
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if (response.body()!!.status == 200) {
                            Toast.makeText(context, "식재료 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
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
                API.patchIngredientData(accessToken, fridgeId, patchData).enqueue(object: Callback<StatusResponse>{
                    override fun onResponse(
                        call: Call<StatusResponse>,
                        response: Response<StatusResponse>
                    ) {
                        if (response.body()!!.status == 200) {
                            Toast.makeText(context, "식재료 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
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

                API.getFridgeData(accessToken).enqueue(object: Callback<MyIngredList> {
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
                        }
                    }

                    override fun onFailure(call: Call<MyIngredList>, t: Throwable) {
                        Toast.makeText(context, R.string.err_server, Toast.LENGTH_SHORT).show()
                    }
                })
                refrigeratorRecyclerAdapter.ingredDatas = ingredientDatas
                refrigeratorRecyclerAdapter.notifyDataSetChanged()
            }
        }

        refrigeratorRecyclerAdapter = RefrigeratorRecyclerAdapter(onDialogRecyclerViewItemClickListener)
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

        refrigeratorRecyclerAdapter.ingredDatas = ingredDatas
        refrigeratorRecyclerAdapter.notifyDataSetChanged()

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
    fun postIngredient(ingredId: Int, expiredAt: String, quantity: Int)
    fun patchIngredient(fridgeId: Int, ingredId: Int, expiredAt: String, quantity: Int)
    fun deleteIngredient(fridgeId: Int)
    fun getIngredient()
}