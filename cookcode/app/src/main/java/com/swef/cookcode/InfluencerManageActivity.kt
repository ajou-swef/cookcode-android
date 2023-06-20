package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.RequestInfluencerRecyclerviewAdapter
import com.swef.cookcode.data.AuthorityRequestData
import com.swef.cookcode.data.GlobalVariables.adminAPI
import com.swef.cookcode.data.response.AuthorityRequestUser
import com.swef.cookcode.data.response.AuthorizationResponse
import com.swef.cookcode.databinding.ActivityInfluencerManageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InfluencerManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfluencerManageBinding

    private lateinit var requestInfluencerRecyclerviewAdapter: RequestInfluencerRecyclerviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfluencerManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestInfluencerRecyclerviewAdapter = RequestInfluencerRecyclerviewAdapter(this)
        binding.recyclerView.apply {
            adapter = requestInfluencerRecyclerviewAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        getAuthorityRequests()
    }

    override fun onResume() {
        super.onResume()
        binding.beforeArrow.setOnClickListener {
            finish()
        }
    }

    private fun getAuthorityRequests() {
        adminAPI.getAuthorization().enqueue(object : Callback<AuthorizationResponse> {
            override fun onResponse(
                call: Call<AuthorizationResponse>,
                response: Response<AuthorizationResponse>
            ) {
                if (response.isSuccessful) {
                    val authorityRequestDatas = getAuthorityRequestsFromResponse(response.body()!!.authorityRequest)
                    if (authorityRequestDatas.isNotEmpty()) {
                        binding.noExist.visibility = View.GONE
                        requestInfluencerRecyclerviewAdapter.datas = authorityRequestDatas as MutableList<AuthorityRequestData>
                        requestInfluencerRecyclerviewAdapter.notifyDataSetChanged()
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<AuthorizationResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getAuthorityRequestsFromResponse(requests: List<AuthorityRequestUser>): List<AuthorityRequestData> {
        val authorityRequestDatas = mutableListOf<AuthorityRequestData>()

        for (item in requests) {
            val userId = item.userId
            val authority = item.authority
            val createdAt = item.createdAt.substring(0, 10)

            authorityRequestDatas.add(AuthorityRequestData(userId, authority, createdAt))
        }

        return authorityRequestDatas
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}