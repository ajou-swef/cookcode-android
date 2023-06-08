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
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.data.UserData
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.SearchUserRecyclerviewItemBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserRecyclerviewAdapter(
    private val context: Context
): RecyclerView.Adapter<SearchUserRecyclerviewAdapter.ViewHolder>() {
    private val ERR_USER_CODE = -1

    var datas = mutableListOf<UserData>()
    var userId = ERR_USER_CODE

    lateinit var accessToken: String
    lateinit var refreshToken: String

    private val accountAPI = AccountAPI.create()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchUserRecyclerviewAdapter.ViewHolder {
        val binding = SearchUserRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchUserRecyclerviewAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: SearchUserRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item : UserData){
            binding.userName.text = item.nickname
            binding.subscribeUsers.text = context.getString(R.string.subscribe_users, item.subscriberCount)
            if (item.profileImage != null){
                getImageFromUrl(item.profileImage, binding.profileImage)
            }

            binding.layout.setOnClickListener {
                startUserPageActivity(item.userId)
            }

            changeButtonSubscribed(item.subscribed)

            binding.btnSubscribe.setOnClickListener {
                if (item.subscribed) {
                    postSubscribe()
                    item.subscribed = false
                    changeButtonSubscribed(false)
                }
                else {
                    postSubscribe()
                    item.subscribed = true
                    changeButtonSubscribed(true)
                }
            }
        }

        private fun postSubscribe() {
            accountAPI.postUserSubscribe(accessToken, userId).enqueue(object :
                Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (!response.isSuccessful){
                        Log.d("data_size", call.request().toString())
                        Log.d("data_size", response.errorBody()!!.string())
                        putToastMessage("에러 발생!")
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    putToastMessage("잠시 후 다시 시도해주세요.")
                }
            })
        }

        private fun changeButtonSubscribed(subscribed: Boolean) {
            if (subscribed) {
                binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component)
                binding.btnSubscribe.text = "구독중"
            }
            else {
                binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component_clicked)
                binding.btnSubscribe.text = "구독하기"
            }
        }

        private fun startUserPageActivity(madeUserId: Int) {
            val nextIntent = Intent(context, UserPageActivity::class.java)
            nextIntent.putExtra("access_token", accessToken)
            nextIntent.putExtra("refresh_token", refreshToken)
            nextIntent.putExtra("my_user_id", userId)
            nextIntent.putExtra("user_id", madeUserId)
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(nextIntent)
        }
    }

    fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .into(view)
    }

    fun putToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}