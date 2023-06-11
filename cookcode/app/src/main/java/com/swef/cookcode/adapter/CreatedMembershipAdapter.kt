package com.swef.cookcode.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.GlobalVariables.membershipAPI
import com.swef.cookcode.data.response.Membership
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.PremiumMembershipItemBinding
import com.swef.cookcode.`interface`.MembershipSigninListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreatedMembershipAdapter(
    private val context: Context,
    private val listener: MembershipSigninListener
): RecyclerView.Adapter<CreatedMembershipAdapter.ViewHolder>() {

    private lateinit var binding: PremiumMembershipItemBinding

    var datas = mutableListOf<Membership>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CreatedMembershipAdapter.ViewHolder {
        binding = PremiumMembershipItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreatedMembershipAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: PremiumMembershipItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Membership) {
            binding.grade.text = item.grade
            binding.price.text = context.getString(R.string.membership_price, item.price.toString())

            binding.btnSignin.setOnClickListener {
                signinMembership(item.membershipId)
            }
        }

        private fun signinMembership(membershipId: Int) {
            membershipAPI.postSigninMembership(membershipId).enqueue(object : Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful) {
                        putToastMessage("가입이 완료되었습니다.")
                        listener.signinSuccess()
                    }
                    else {
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", response.errorBody()!!.string())
                        putToastMessage("에러 발생!")
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", t.message.toString())
                    putToastMessage("잠시 후 다시 시도해주세요.")
                }

            })
        }
    }

    fun putToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}