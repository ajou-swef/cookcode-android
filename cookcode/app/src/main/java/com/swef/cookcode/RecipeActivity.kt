package com.swef.cookcode

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.swef.cookcode.adapter.CommentRecyclerviewAdapter
import com.swef.cookcode.adapter.RecipeViewpagerAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.CommentData
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.response.Comment
import com.swef.cookcode.data.response.CommentResponse
import com.swef.cookcode.data.response.Photos
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeContentResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.Step
import com.swef.cookcode.data.response.Videos
import com.swef.cookcode.databinding.ActivityRecipeBinding
import com.swef.cookcode.`interface`.CommentOnClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeActivity : AppCompatActivity(), CommentOnClickListener {
    private lateinit var binding: ActivityRecipeBinding

    private val ERR_RECIPE_ID = -1
    private val ERR_USER_CODE = -1

    private val API = RecipeAPI.create()

    private lateinit var recipeViewpagerAdapter: RecipeViewpagerAdapter
    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private var userId = ERR_USER_CODE
    private var recipeId = ERR_RECIPE_ID

    private lateinit var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback

    private lateinit var commentRecyclerviewAdapter: CommentRecyclerviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)
        recipeId = intent.getIntExtra("recipe_id", ERR_RECIPE_ID)

        binding.btnDelete.setOnClickListener {
            // 레시피 삭제 Dialog
            deleteRecipeDialog(binding.btnDelete)
        }

        binding.btnModify.setOnClickListener {
            // 레시피 수정 activity
            startModifyRecipeActivity()
        }

        recipeViewpagerAdapter = RecipeViewpagerAdapter(this)
        recipeViewpagerAdapter.accessToken = accessToken
        binding.viewpager.adapter = recipeViewpagerAdapter

        getRecipeDataFromRecipeID(recipeId, accessToken)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        initBottomSheetCallback()

        val persistentBottomSheet = BottomSheetBehavior.from(binding.bottomSheet)
        persistentBottomSheet.addBottomSheetCallback(bottomSheetCallback)

        binding.btnConfirm.setOnClickListener {
            putToastMessage("정상적으로 등록 되었습니다.")
            binding.editComment.text = null
        }

        binding.bottomSheet.setOnClickListener{
            if (persistentBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
                persistentBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            else
                persistentBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED

        }

        binding.bottomSheet.setOnFocusChangeListener { view, hasFocus ->
            hideKeyboardFromEditText(view, hasFocus, this)
            persistentBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        initCommentRecyclerview()
        initCommentConfirmButton()
    }

    private fun hideKeyboardFromEditText(view: View, hasFocus: Boolean, context: Context) {
        val hideFlags = 0
        if (!hasFocus) {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, hideFlags)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val hideFlags = 0
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)

                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), hideFlags)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getRecipeDataFromRecipeID(recipeId: Int, accessToken: String) {
        API.getRecipe(accessToken, recipeId).enqueue(object : Callback<RecipeContentResponse> {
            override fun onResponse(
                call: Call<RecipeContentResponse>,
                response: Response<RecipeContentResponse>
            ) {
                if (response.body() != null) {
                    val recipeAndStepData = getRecipeDataFromResponseBody(response.body()!!.recipeData)

                    if (userId == response.body()!!.recipeData.user.userId) {
                        setButtonVisibility(true)
                    }
                    else {
                        setButtonVisibility(false)
                    }
                    recipeViewpagerAdapter.setData(recipeAndStepData)
                }
                else {
                    putToastMessage("데이터를 불러오는데 실패했습니다.")
                    Log.d("data_size", response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<RecipeContentResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun setButtonVisibility(type: Boolean){
        if (type) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnModify.visibility = View.VISIBLE
        }
        else {
            binding.btnDelete.visibility = View.GONE
            binding.btnModify.visibility = View.GONE
        }
    }

    private fun getRecipeDataFromResponseBody(data: RecipeContent): RecipeAndStepData {
        val recipeAndStepData: RecipeAndStepData

        val recipeData = RecipeData(
            data.recipeId, data.title, data.description,
            data.mainImage, data.likeCount, data.isLiked, data.isCookable,
            data.user, data.createdAt, data.ingredients, data.additionalIngredients)
        val stepDatas = getStepDatasFromRecipeContent(data.steps)

        recipeAndStepData = RecipeAndStepData(recipeData, stepDatas)

        return recipeAndStepData
    }

    private fun getStepDatasFromRecipeContent(datas: List<Step>): MutableList<StepData> {
        val stepDatas = mutableListOf<StepData>()

        for (item in datas) {
            stepDatas.apply {
                val imageUris = getImageDatasFromStep(item.imageUris)
                val videoUris = getVideoDatasFromStep(item.videoUris)
                val title = item.title
                val description = item.description
                val numberOfStep = item.sequence

                add(StepData(imageUris, videoUris, title, description, numberOfStep))
            }
        }

        return stepDatas
    }

    private fun getImageDatasFromStep(datas: List<Photos>): MutableList<String> {
        val imageUris = mutableListOf<String>()

        for (item in datas) {
            imageUris.add(item.imageUri)
        }

        return imageUris
    }

    private fun getVideoDatasFromStep(datas: List<Videos>): MutableList<String> {
        val videoUris = mutableListOf<String>()

        for (item in datas) {
            videoUris.add(item.videoUri)
        }

        return videoUris
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun deleteRecipeDialog(view: View) {
        AlertDialog.Builder(view.context).apply {
            setTitle("레시피 삭제")
            setMessage("정말 삭제 하시겠습니까?")
            setPositiveButton("삭제") { _, _ ->
                deleteRecipe()
            }
            setNegativeButton("취소") { _, _ -> /* Do nothing */ }
            show()
        }
    }

    private fun deleteRecipe() {
        API.deleteRecipe(accessToken, recipeId).enqueue(object: Callback<StatusResponse>{
            override fun onResponse(call: Call<StatusResponse>,response: Response<StatusResponse>) {
                if (response.isSuccessful){
                    putToastMessage("정상적으로 삭제 되었습니다.")
                    finish()
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun startModifyRecipeActivity() {
        val nextIntent = Intent(this, RecipeFormActivity::class.java)
        nextIntent.putExtra("access_token", accessToken)
        nextIntent.putExtra("refresh_token", refreshToken)
        nextIntent.putExtra("recipe_id", recipeId)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(nextIntent)
    }

    private fun initBottomSheetCallback() {
        bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            // bottom sheet의 상태값 변경
            override fun onStateChanged(bottomSheet: View, newState: Int) { /* Do Nothing */ }

            // botton sheet가 스크롤될 때 호출
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset >= 0) {
                    binding.guideArrow.rotation = (1 - slideOffset) * 180F - 90F
                }
            }
        }
    }

    private fun initCommentRecyclerview(){
        val linearLayoutManager = LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, false)
        commentRecyclerviewAdapter = CommentRecyclerviewAdapter(this, "recipe", this)
        binding.commentRecyclerview.apply {
            adapter = commentRecyclerviewAdapter
            layoutManager = linearLayoutManager
        }

        initOnScrollListener(linearLayoutManager)
        getRecipeComments(recipeId)
    }

    private fun getRecipeComments(recipeId: Int){
        API.getRecipeComments(accessToken, recipeId).enqueue(object: Callback<CommentResponse>{
            override fun onResponse(
                call: Call<CommentResponse>,
                response: Response<CommentResponse>
            ) {
                if (response.isSuccessful) {
                    val comments = getCommentFromResponse(response.body()!!.content.comments)

                    commentRecyclerviewAdapter.userId = userId
                    commentRecyclerviewAdapter.accessToken = accessToken
                    commentRecyclerviewAdapter.recipeId = recipeId

                    if (comments.isEmpty()) {
                        binding.noExistComments.visibility = View.VISIBLE
                        commentRecyclerviewAdapter.notifyDataSetChanged()
                    }
                    else {
                        if (commentRecyclerviewAdapter.datas.isEmpty()) {
                            commentRecyclerviewAdapter.datas = comments as MutableList<CommentData>
                            commentRecyclerviewAdapter.notifyItemRangeChanged(0, comments.size)
                        }
                        else {
                            val beforeSize = commentRecyclerviewAdapter.itemCount
                            commentRecyclerviewAdapter.datas.addAll(comments)
                            commentRecyclerviewAdapter.notifyItemRangeChanged(beforeSize, beforeSize + comments.size)
                        }
                        binding.noExistComments.visibility = View.GONE
                    }
                }
                else {
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                }
            }

            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Log.d("data_size", t.message.toString())
                Log.d("data_size", call.request().toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getCommentFromResponse(response: List<Comment>): List<CommentData>{
        val comments = mutableListOf<CommentData>()

        for (item in response){
            comments.apply {
                add(CommentData(item.madeUser, item.comment, item.commentId))
            }
        }

        return comments
    }

    override fun itemOnClick(id: Int) {
        commentRecyclerviewAdapter.datas.clear()
        getRecipeComments(id)
    }

    private fun initCommentConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            commentRecyclerviewAdapter.datas.clear()
            putRecipeComment(binding.editComment.text.toString())
        }
    }

    private fun putRecipeComment(comment: String) {
        val body = HashMap<String, String>()
        body["comment"] = comment

        API.putRecipeComment(accessToken, recipeId, body).enqueue(object : Callback<StatusResponse> {
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {
                if (response.isSuccessful) {
                    putToastMessage("댓글이 정상적으로 등록되었습니다.")
                    getRecipeComments(recipeId)
                } else {
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

    private fun initOnScrollListener(linearLayoutManager: LinearLayoutManager) {
        binding.commentRecyclerview.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)

                if(differentY > 0) {
                    val visibleItemCount = linearLayoutManager.childCount
                    val totalItemCount = linearLayoutManager.itemCount
                    val pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        getRecipeComments(recipeId)
                    }
                }
            }
        })
    }
}