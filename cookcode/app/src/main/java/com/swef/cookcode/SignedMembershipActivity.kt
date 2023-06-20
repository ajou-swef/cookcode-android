package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.SignedMembershipAdapter
import com.swef.cookcode.data.GlobalVariables.membershipAPI
import com.swef.cookcode.data.response.SignedMembership
import com.swef.cookcode.data.response.SignedMembershipResponse
import com.swef.cookcode.databinding.ActivitySignedMembershipBinding
import com.swef.cookcode.`interface`.MembershipDeleteListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignedMembershipActivity : AppCompatActivity(), MembershipDeleteListener {
    private lateinit var binding: ActivitySignedMembershipBinding

    private lateinit var signedMembershipAdapter: SignedMembershipAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignedMembershipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signedMembershipAdapter = SignedMembershipAdapter(this, this)
        binding.recyclerView.apply {
            adapter = signedMembershipAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        getSignedMemberships()
    }

    override fun onResume() {
        super.onResume()
        binding.beforeArrow.setOnClickListener {
            finish()
        }
    }

    private fun getSignedMemberships() {
        membershipAPI.getSignedMemberships().enqueue(object : Callback<SignedMembershipResponse> {
            override fun onResponse(
                call: Call<SignedMembershipResponse>,
                response: Response<SignedMembershipResponse>
            ) {
                if (response.isSuccessful) {
                    val datas = response.body()!!.membership
                    if (datas.isNotEmpty()) {
                        binding.noExist.visibility = View.GONE
                        signedMembershipAdapter.datas = datas as MutableList<SignedMembership>
                        signedMembershipAdapter.notifyDataSetChanged()
                    }

                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<SignedMembershipResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }

        })
    }

    override fun deleteSuccess() {
        getSignedMemberships()
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}