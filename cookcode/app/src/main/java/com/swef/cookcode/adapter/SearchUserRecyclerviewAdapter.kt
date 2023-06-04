package com.swef.cookcode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.UserData
import com.swef.cookcode.databinding.SearchUserRecyclerviewItemBinding

class SearchUserRecyclerviewAdapter(
    private val context: Context
): RecyclerView.Adapter<SearchUserRecyclerviewAdapter.ViewHolder>() {
    private val ERR_USER_CODE = -1

    var datas = mutableListOf<UserData>()
    var userId = ERR_USER_CODE

    lateinit var accessToken: String
    lateinit var refreshToken: String

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
            binding.subscribeUsers.text = context.getString(R.string.subscribe_users, item.userId)
        }
    }
}