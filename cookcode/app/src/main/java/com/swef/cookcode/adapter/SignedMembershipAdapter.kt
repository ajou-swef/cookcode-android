package com.swef.cookcode.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.R
import com.swef.cookcode.UserPageActivity
import com.swef.cookcode.data.GlobalVariables.membershipAPI
import com.swef.cookcode.data.response.MadeUser
import com.swef.cookcode.data.response.SignedMembership
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.SignedPremiumMembershipItemBinding
import com.swef.cookcode.`interface`.MembershipDeleteListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignedMembershipAdapter(
    private val context: Context,
    private val listener: MembershipDeleteListener
): RecyclerView.Adapter<SignedMembershipAdapter.ViewHolder>() {

    private lateinit var binding: SignedPremiumMembershipItemBinding

    var datas = mutableListOf<SignedMembership>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SignedMembershipAdapter.ViewHolder {
        binding = SignedPremiumMembershipItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SignedMembershipAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: SignedPremiumMembershipItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SignedMembership) {
            binding.grade.text = item.grade
            binding.price.text = context.getString(R.string.membership_price, item.price.toString())
            binding.nickname.text = item.creater.nickname
            if (item.creater.profileImageUri != null) {
                getImageFromUrl(item.creater.profileImageUri, binding.profileImage)
            }

            binding.nickname.setOnClickListener {
                startUserPageActivity(item.creater)
            }
            binding.profileImage.setOnClickListener{
                startUserPageActivity(item.creater)
            }

            binding.btnSignout.setOnClickListener {
                deleteMembership(item.membershipId)
            }
        }

        private fun deleteMembership(membershipId: Int) {
            membershipAPI.deleteMembership(membershipId).enqueue(object : Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful) {
                        putToastMessage("멤버십 탈퇴가 완료되었습니다.")
                        listener.deleteSuccess()
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

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .into(view)
    }

    private fun startUserPageActivity(madeUser: MadeUser) {
        val nextIntent = Intent(context, UserPageActivity::class.java)
        nextIntent.putExtra("user_id", madeUser.userId)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(nextIntent)
    }

    fun putToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}