package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import com.swef.cookcode.adapter.RecipeViewpagerAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.response.Photos
import com.swef.cookcode.data.response.RecipeContent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val accessToken = intent.getStringExtra("access_token")!!
        val recipeId = intent.getIntExtra("recipe_id", ERR_RECIPE_ID)

        val data = getRecipeDataFromRecipeID(recipeId, accessToken)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        val recipeViewpagerAdapter = RecipeViewpagerAdapter()
        recipeViewpagerAdapter.recipeData = data.recipeData
        recipeViewpagerAdapter.stepDatas = data.stepData
        binding.viewpager.adapter = recipeViewpagerAdapter
    }

    private fun getRecipeDataFromRecipeID(recipeId: Int, accessToken: String): RecipeAndStepData {
        lateinit var recipeAndStepData: RecipeAndStepData

        API.getRecipe(accessToken, recipeId).enqueue(object: Callback<RecipeContent> {
            override fun onResponse(call: Call<RecipeContent>, response: Response<RecipeContent>) {
                if (response.isSuccessful) {
                    recipeAndStepData = getRecipeDataFromResponseBody(response.body()!!)
                }
            }

            override fun onFailure(call: Call<RecipeContent>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })

        return recipeAndStepData
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
}