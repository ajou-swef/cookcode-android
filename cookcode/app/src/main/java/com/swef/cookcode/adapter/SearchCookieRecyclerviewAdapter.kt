package com.swef.cookcode.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.CookieActivity
import com.swef.cookcode.R
import com.swef.cookcode.data.SearchCookieData
import com.swef.cookcode.databinding.CookieThumbnailItemBinding

class SearchCookieRecyclerviewAdapter(
    private val context: Context
): RecyclerView.Adapter<SearchCookieRecyclerviewAdapter.ViewHolder>() {

    private val ERR_USER_CODE = -1

    private lateinit var binding: CookieThumbnailItemBinding
    var datas = mutableListOf<SearchCookieData>()

    var viewWidth = 0
    var viewHeight = 0

    var userId = ERR_USER_CODE
    lateinit var accessToken: String
    lateinit var refreshToken: String

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchCookieRecyclerviewAdapter.ViewHolder {
        binding = CookieThumbnailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.layout.layoutParams.width = viewWidth
        binding.layout.layoutParams.height = viewHeight
        return ViewHolder(binding, parent)
    }

    override fun onBindViewHolder(
        holder: SearchCookieRecyclerviewAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding : CookieThumbnailItemBinding,
        private val parent: ViewGroup
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchCookieData) {
            getImageFromUrl(item.thumbnail, binding.thumbnail)
            binding.likeNumber.text =
                parent.context.getString(R.string.like_number, item.likeNumber)

            binding.layout.setOnClickListener {
                startCookieActivity(item.cookieId, item.madeUserId)
            }
        }

        private fun startCookieActivity(cookieId: Int, madeUserId: Int) {
            val intent = Intent(context, CookieActivity::class.java)
            intent.putExtra("cookie_id", cookieId)
            intent.putExtra("user_id", userId)
            intent.putExtra("made_user_id", madeUserId)
            intent.putExtra("access_token", accessToken)
            intent.putExtra("refresh_token", refreshToken)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        if (imageUrl != "") {
            Glide.with(context)
                .load(imageUrl)
                .into(view)
        }
        else {
            view.setImageResource(R.drawable.upload_image)
        }
    }
}