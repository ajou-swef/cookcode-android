package com.swef.cookcode.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.response.Membership
import com.swef.cookcode.databinding.PremiumMembershipManageItemBinding

class CreateMembershipAdapter(
    private val context: Context
): RecyclerView.Adapter<CreateMembershipAdapter.ViewHolder>() {

    private lateinit var binding: PremiumMembershipManageItemBinding

    var datas = mutableListOf<Membership>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CreateMembershipAdapter.ViewHolder {
        binding = PremiumMembershipManageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreateMembershipAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: PremiumMembershipManageItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Membership) {
            binding.grade.text = item.grade
            binding.price.text = context.getString(R.string.membership_price, item.price.toString())
        }
    }
}