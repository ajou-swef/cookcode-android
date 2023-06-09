package com.swef.cookcode.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.R
import com.swef.cookcode.RecipeActivity
import com.swef.cookcode.UserPageActivity
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.databinding.SearchRecipeRecyclerviewItemBinding

class SearchRecipeRecyclerviewAdapter(
    private val context: Context
): RecyclerView.Adapter<SearchRecipeRecyclerviewAdapter.ViewHolder>() {
    private lateinit var binding: SearchRecipeRecyclerviewItemBinding

    var datas = mutableListOf<RecipeData>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchRecipeRecyclerviewAdapter.ViewHolder {
        binding = SearchRecipeRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SearchRecipeRecyclerviewAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(
        private val binding: SearchRecipeRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecipeData){
            binding.recipeName.text = context.getString(
                R.string.string_shadow_convert, item.title)
            binding.likeNumber.text = item.likes.toString()
            binding.madeUser.text = item.madeUser.nickname
            binding.createdAtTime.text = item.createdAt
            getImageFromUrl(item.mainImage, binding.mainImage)

            if (item.madeUser.profileImageUri != null){
                getImageFromUrl(item.madeUser.profileImageUri, binding.userProfileImage)
            }

            if (item.cookable) {
                binding.isCookable.visibility = View.VISIBLE
            }
            else {
                binding.isCookable.visibility = View.INVISIBLE
            }

            binding.layout.setOnClickListener {
                val intent = Intent(binding.layout.context, RecipeActivity::class.java)
                intent.putExtra("recipe_id", item.recipeId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                binding.layout.context.startActivity(intent)
            }

            if (item.isLiked) {
                binding.likeMark.setBackgroundResource(R.drawable.icon_liked)
            }
            else {
                binding.likeMark.setBackgroundResource(R.drawable.icon_unliked)
            }

            binding.madeUser.setOnClickListener {
                startUserPageActivity(item.madeUser.userId)
            }
            binding.userProfileImage.setOnClickListener {
                startUserPageActivity(item.madeUser.userId)
            }
        }
    }

    private fun startUserPageActivity(madeUserId: Int) {
        val nextIntent = Intent(context, UserPageActivity::class.java)
        nextIntent.putExtra("user_id", madeUserId)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(nextIntent)
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .into(view)

        binding.mainImage.clipToOutline = true
    }
}