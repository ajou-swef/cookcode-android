package com.swef.cookcode.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.swef.cookcode.R
import com.swef.cookcode.data.SearchCookieData
import com.swef.cookcode.databinding.CookieThumbnailItemBinding

class SearchCookieRecyclerviewAdapter(
    private val context: Context
): RecyclerView.Adapter<SearchCookieRecyclerviewAdapter.ViewHolder>() {

    private lateinit var binding: CookieThumbnailItemBinding
    var datas = mutableListOf<SearchCookieData>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchCookieRecyclerviewAdapter.ViewHolder {
        binding = CookieThumbnailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        }
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        // 현재 썸네일 안보내주므로 해당 코드는 imageUrl이 왔을때 사용
        /*
        Glide.with(context)
            .load(imageUrl)
            .into(view)
         */

        // 지금은 동영상만 오므로 영상에서 썸네일을 직접 추출
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(imageUrl)

        val options = RequestOptions()
            .frame(0) // 0초의 썸네일을 가져옵니다.

        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .apply(options)
            .into(view)
    }
}