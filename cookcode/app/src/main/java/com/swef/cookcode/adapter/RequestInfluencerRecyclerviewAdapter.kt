package com.swef.cookcode.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.R
import com.swef.cookcode.data.AuthorityRequestData
import com.swef.cookcode.data.GlobalVariables.FALSE
import com.swef.cookcode.data.GlobalVariables.TRUE
import com.swef.cookcode.data.GlobalVariables.adminAPI
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.RequestInfluencerItemBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestInfluencerRecyclerviewAdapter(
    private val context: Context
): RecyclerView.Adapter<RequestInfluencerRecyclerviewAdapter.ViewHolder>() {
    private lateinit var binding: RequestInfluencerItemBinding

    var datas = mutableListOf<AuthorityRequestData>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RequestInfluencerRecyclerviewAdapter.ViewHolder {
        binding = RequestInfluencerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RequestInfluencerRecyclerviewAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind(datas[position], position)
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: RequestInfluencerItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AuthorityRequestData, position: Int) {
            binding.userId.text = context.getString(R.string.request_user_id, item.userId)
            binding.createdAt.text = context.getString(R.string.request_created_at, item.createdAt)

            binding.btnAccept.setOnClickListener {
                patchAuthorization(item.userId, TRUE, position)
            }
            binding.btnDeny.setOnClickListener {
                patchAuthorization(item.userId, FALSE, position)
            }
        }

        private fun patchAuthorization(userId: Int, isAccept: Int, position: Int) {
            adminAPI.patchAuthorization(userId, isAccept).enqueue(object: Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful) {
                        putToastMessage("요청이 완료되었습니다.")
                        deleteSuccessData(position)
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

        private fun deleteSuccessData(position: Int) {
            datas.removeAt(position)
            notifyItemRemoved(position)
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