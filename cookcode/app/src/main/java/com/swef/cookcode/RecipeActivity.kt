package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.swef.cookcode.adapter.RecipeViewpagerAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.response.Photos
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeContentResponse
import com.swef.cookcode.data.response.Step
import com.swef.cookcode.data.response.Videos
import com.swef.cookcode.databinding.ActivityRecipeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeBinding

    private val ERR_RECIPE_ID = -1

    private val API = RecipeAPI.create()

    private lateinit var recipeViewpagerAdapter: RecipeViewpagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val accessToken = intent.getStringExtra("access_token")!!
        val recipeId = intent.getIntExtra("recipe_id", ERR_RECIPE_ID)
        Log.d("data_size", accessToken)
        Log.d("data_size", recipeId.toString())

        recipeViewpagerAdapter = RecipeViewpagerAdapter()
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

    private fun getRecipeDataFromResponseBody(data: RecipeContent): RecipeAndStepData {
        val recipeAndStepData: RecipeAndStepData

        val recipeData = RecipeData(data.recipeId, data.title, data.description, data.mainImage.toUri(), data.likeCount, data.user)
        val stepDatas = getStepDatasFromRecipeContent(data.steps)

        recipeAndStepData = RecipeAndStepData(recipeData, stepDatas)

        return recipeAndStepData
    }

    private fun getStepDatasFromRecipeContent(datas: List<Step>): MutableList<StepData> {
        val stepDatas = mutableListOf<StepData>()

        // mock data
        stepDatas.apply {
            val imageUri = mutableListOf<String>()
            imageUri.add("https://picsum.photos/200/300")
            val videoUri = null
            val title = "요리 만들기"
            val description = "쿡코드를 연다"
            val seq = 1

            add(StepData(imageUri, videoUri, title, description, seq))
        }
        /*
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

         */

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
}