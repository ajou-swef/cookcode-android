package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.swef.cookcode.adapter.RecipeViewpagerAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.response.Photos
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeContentResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.Step
import com.swef.cookcode.data.response.Videos
import com.swef.cookcode.databinding.ActivityRecipeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeBinding

    private val ERR_RECIPE_ID = -1
    private val ERR_USER_CODE = -1

    private val API = RecipeAPI.create()

    private lateinit var recipeViewpagerAdapter: RecipeViewpagerAdapter

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = ERR_USER_CODE
    private var recipeId = ERR_RECIPE_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accessToken = intent.getStringExtra("access_token")!!
        refreshToken = intent.getStringExtra("refresh_token")!!
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)
        recipeId = intent.getIntExtra("recipe_id", ERR_RECIPE_ID)

        Log.d("data_size", userId.toString())

        binding.btnDelete.setOnClickListener {
            // 레시피 삭제 Dialog
            deleteRecipeDialog(binding.btnDelete)
        }

        binding.btnModify.setOnClickListener {
            // 레시피 수정 activity
            startModifyRecipeActivity()
        }

        recipeViewpagerAdapter = RecipeViewpagerAdapter(this)
        binding.viewpager.adapter = recipeViewpagerAdapter

        getRecipeDataFromRecipeID(recipeId, accessToken)

        binding.beforeArrow.setOnClickListener {
            finish()
        }
    }


    private fun getRecipeDataFromRecipeID(recipeId: Int, accessToken: String) {
        API.getRecipe(accessToken, recipeId).enqueue(object : Callback<RecipeContentResponse> {
            override fun onResponse(
                call: Call<RecipeContentResponse>,
                response: Response<RecipeContentResponse>
            ) {
                if (response.body() != null) {
                    val recipeAndStepData = getRecipeDataFromResponseBody(response.body()!!.recipeData)
                    Log.d("data_size", response.body().toString())

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

        val recipeData = RecipeData(data.recipeId, data.title, data.description, data.mainImage, data.likeCount, data.isCookable, data.user)
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
}