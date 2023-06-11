package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.CreateMembershipAdapter
import com.swef.cookcode.data.GlobalVariables.membershipAPI
import com.swef.cookcode.data.GlobalVariables.userId
import com.swef.cookcode.data.response.Membership
import com.swef.cookcode.data.response.MembershipResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.ActivityMyMembershipCreateBinding
import com.swef.cookcode.databinding.MembershipAddDialogBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyMembershipCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyMembershipCreateBinding

    private lateinit var membershipCreateDialogView: MembershipAddDialogBinding
    private lateinit var membershipCreateDialog: AlertDialog

    private lateinit var createMembershipAdapter: CreateMembershipAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyMembershipCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        createMembershipAdapter = CreateMembershipAdapter(this)
        binding.recyclerView.apply {
            adapter = createMembershipAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        getMyMemberships()

        membershipCreateDialogView = MembershipAddDialogBinding.inflate(layoutInflater)
        membershipCreateDialog = AlertDialog.Builder(this)
            .setView(membershipCreateDialogView.root)
            .create()

        membershipCreateDialogView.btnCancel.setOnClickListener {
            membershipCreateDialog.dismiss()
        }

        membershipCreateDialogView.btnConfirm.setOnClickListener {
            val gradeName = membershipCreateDialogView.editMembershipName.text.toString()
            val price = membershipCreateDialogView.editPrice.text.toString()

            val body = HashMap<String, String>()
            body["grade"] = gradeName
            body["price"] = price

            postMembership(body)
        }
    }

    private fun postMembership(body: HashMap<String, String>) {
        membershipAPI.postCreaterMembership(body).enqueue(object : Callback<StatusResponse> {
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {
                if (response.isSuccessful) {
                    putToastMessage("멤버십 등록 완료")
                    membershipCreateDialog.dismiss()
                    getMyMemberships()
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }

        })
    }

    private fun getMyMemberships() {
        membershipAPI.getCreatersMemberships(userId).enqueue(object : Callback<MembershipResponse> {
            override fun onResponse(
                call: Call<MembershipResponse>,
                response: Response<MembershipResponse>
            ) {
                if (response.isSuccessful) {
                    val datas = response.body()!!.membership
                    createMembershipAdapter.datas = datas as MutableList<Membership>
                    createMembershipAdapter.notifyDataSetChanged()
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<MembershipResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        binding.btnAdd.setOnClickListener {
            membershipCreateDialog.show()
        }
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}