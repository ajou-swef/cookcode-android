package com.swef.cookcode.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.R
import com.swef.cookcode.data.CookieData
import com.swef.cookcode.databinding.CookiePreviewItemBinding

class CookieViewpagerAdapter() : RecyclerView.Adapter<CookieViewpagerAdapter.ViewHolder>() {

    var datas = mutableListOf<CookieData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CookiePreviewItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(
        private val binding: CookiePreviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CookieData) {
            binding.cookie.setBackgroundResource(R.drawable.loading_video_page)
            binding.progressBar.visibility = View.VISIBLE
            binding.cookie.setVideoURI(Uri.parse(item.videoUrl))
            val mediaController = MediaController(binding.cookie.context, false)
            mediaController.setAnchorView(binding.cookie)
            binding.cookie.setMediaController(mediaController)

            binding.cookie.setOnPreparedListener { mediaPlayer ->
                binding.cookie.setBackgroundResource(0)
                binding.progressBar.visibility = View.GONE
                mediaPlayer.start()
            }

            binding.cookie.setOnClickListener{
                binding.cookie.start()
            }

            binding.createdAt.text = item.createdAt
            binding.madeUser.text = item.madeUser.nickname

            initLikeButton(item.isLiked)

            binding.btnLike.setOnClickListener {
                initLikeButtonOnClick(item)
            }
        }

        private fun initLikeButton(like: Boolean) {
            if (like){
                binding.btnLike.setBackgroundResource(R.drawable.btn_liked)
            }
            else {
                binding.btnLike.setBackgroundResource(R.drawable.btn_unliked)
            }
        }

        private fun initLikeButtonOnClick(item: CookieData) {
            if(item.isLiked) {
                binding.btnLike.setBackgroundResource(R.drawable.btn_unliked)
                item.isLiked = false
            }
            else {
                binding.btnLike.setBackgroundResource(R.drawable.btn_liked)
                item.isLiked = true
            }
        }

    }


}