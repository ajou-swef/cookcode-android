package com.swef.cookcode.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.data.CommentData
import com.swef.cookcode.data.GlobalVariables.ERR_CODE
import com.swef.cookcode.data.GlobalVariables.cookieAPI
import com.swef.cookcode.data.GlobalVariables.recipeAPI
import com.swef.cookcode.data.GlobalVariables.userId
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.databinding.CommentModifyDialogBinding
import com.swef.cookcode.databinding.CommentRecyclerviewItemBinding
import com.swef.cookcode.`interface`.CommentOnClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentRecyclerviewAdapter(
    private val context: Context,
    private val type: String,
    private val listener: CommentOnClickListener
):RecyclerView.Adapter<CommentRecyclerviewAdapter.ViewHolder>() {

    var datas = mutableListOf<CommentData>()
    var cookieId = ERR_CODE
    var recipeId = ERR_CODE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommentRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(
        private val binding: CommentRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentData){
            binding.comment.text = item.comment
            binding.madeUser.text = item.madeUser.nickname

            if (userId == item.madeUser.userId){
                binding.btnModify.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnReport.visibility = View.GONE
            }
            else {
                binding.btnModify.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
                binding.btnReport.visibility = View.VISIBLE
            }

            binding.btnModify.setOnClickListener {
                showCommentModifyDialog(item)
            }
            binding.btnDelete.setOnClickListener {
                showCommentDeleteDialog(item.commentId)
            }
        }

        private fun showCommentModifyDialog(item: CommentData){
            val dialogView = CommentModifyDialogBinding.inflate(LayoutInflater.from(context))
            dialogView.editComment.setText(item.comment)

            val commentModifyDialog = AlertDialog.Builder(context)
                .setView(dialogView.root)
                .create()

            dialogView.btnConfirm.setOnClickListener { /* 댓글 수정 API */
                putToastMessage("댓글이 정상적으로 수정되었습니다.")
                commentModifyDialog.dismiss()
            }
            dialogView.btnCancel.setOnClickListener { commentModifyDialog.dismiss() }

            commentModifyDialog.show()
        }

        private fun showCommentDeleteDialog(commentId: Int){
            val commentDeleteDialog = AlertDialog.Builder(context)
                .setTitle("댓글 삭제")
                .setMessage("정말 삭제하시겠습니까?")
                .setPositiveButton("확인"){ _, _ ->
                    // 댓글 삭제 API
                    if (type == "cookie") {
                        deleteCookieComment(commentId)
                    }
                    else {
                        deleteRecipeComment(commentId)
                    }
                }
                .setNegativeButton("취소"){ _, _ -> }
                .create()

            commentDeleteDialog.show()
        }

        private fun deleteCookieComment(commentId: Int){
            cookieAPI.deleteCookieComment(commentId).enqueue(object :
                Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful){
                        putToastMessage("댓글이 삭제되었습니다.")
                        listener.itemOnClick(cookieId)
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

        private fun deleteRecipeComment(commentId: Int){
            recipeAPI.deleteRecipeComment(commentId).enqueue(object :
                Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful){
                        putToastMessage("댓글이 삭제되었습니다.")
                        listener.itemOnClick(recipeId)
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