package com.swef.cookcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.swef.cookcode.adapter.StepPreviewRecyclerviewAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.response.RecipeResponse
import com.swef.cookcode.data.response.RecipeStatusResponse
import com.swef.cookcode.databinding.ActivityRecipePreviewBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipePreviewBinding

    private lateinit var stepPreviewRecyclerviewAdapter: StepPreviewRecyclerviewAdapter

    private val API = RecipeAPI.create()

    private val ERR_RECIPE_CODE = -1
    private val ERR_USER_CODE = -1

    private var userId = ERR_USER_CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 레시피 정보 불러오기
        val title = intent.getStringExtra("recipe_title")!!
        val description = intent.getStringExtra("recipe_description")!!
        val mainImage = intent.getStringExtra("main_image")!!
        val essentialIngreds = intent.getStringArrayExtra("essential_ingreds")!!.toList()
        val essentialValues = intent.getStringArrayExtra("essential_values")!!.toList()
        val additionalIngreds = intent.getStringArrayExtra("additional_ingreds")!!.toList()
        val additionalValues = intent.getStringArrayExtra("additional_values")!!.toList()

        val deleteImages = if (intent.getStringArrayExtra("delete_images") != null)
            intent.getStringArrayExtra("delete_images")!!.toList()
        else emptyList<String>()

        val accessToken = intent.getStringExtra("access_token")!!
        val refreshToken = intent.getStringExtra("refresh_token")!!

        val recipeId = intent.getIntExtra("recipe_id", ERR_RECIPE_CODE)
        userId = intent.getIntExtra("user_id", ERR_USER_CODE)

        // 현재 보고있는 step
        var currentPosition = 0

        // step 정보 담아두는 변수
        val datas = mutableListOf<StepData>()

        // 스텝의 개수
        val steps = intent.getIntExtra("index", 0)

        // 각 스텝 별로 step data 저장
        datas.apply {
            for(i: Int in 0 until steps) {
                val stepNumber = intent.getIntExtra("step_number$i", 0)
                val stepImages = intent.getStringArrayExtra("images$i")!!.toList()
                val stepVideos = intent.getStringArrayExtra("videos$i")?.toList()
                val stepTitle = intent.getStringExtra("title$i")!!
                val stepDescription = intent.getStringExtra("description$i")!!

                add(StepData(stepImages, stepVideos, stepTitle, stepDescription, stepNumber))
            }
        }

        stepPreviewRecyclerviewAdapter = StepPreviewRecyclerviewAdapter(datas, this)
        binding.viewpager.apply {
            adapter = stepPreviewRecyclerviewAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // Paging 완료되면 호출
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
            }
        })

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        // 레시피 업로드
        binding.btnUpload.setOnClickListener {
            // 서버에 각 스텝 정보 업로드
            val postBody = HashMap<String, Any>()
            postBody["title"] = title
            postBody["description"] = description
            postBody["ingredients"] = essentialIngreds
            postBody["optionalIngredients"] = additionalIngreds
            postBody["thumbnail"] = mainImage
            postBody["deletedThumbnails"] = deleteImages

            val stepDatas = mutableListOf<HashMap<String, Any>>()

            for(item in datas){
                val stepData = HashMap<String, Any>()

                stepData["seq"] = item.numberOfStep
                stepData["title"] = item.title
                stepData["description"] = item.description
                stepData["photos"] = item.imageData
                stepData["deletedPhotos"] = emptyList<String>()
                stepData["deletedVideos"] = emptyList<String>()
                if(item.videoData != null) stepData["videos"] = item.videoData
                else stepData["videos"] = emptyList<String>()

                stepDatas.add(stepData)
            }

            postBody["steps"] = stepDatas

            if (recipeId != ERR_RECIPE_CODE) {
                patchRecipeData(accessToken, refreshToken, postBody, recipeId)
            }
            else {
                postRecipeData(accessToken, refreshToken, postBody)
            }
        }

        // 미리보기 단계에서 수정
        binding.editButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("step_number", currentPosition)
            setResult(RESULT_OK, intent)
            finish()
        }

    }

    private fun postRecipeData(accessToken: String, refreshToken: String, postBody: HashMap<String, Any>) {
        API.postRecipe(accessToken, postBody).enqueue(object: Callback<RecipeStatusResponse>{
            override fun onResponse(
                call: Call<RecipeStatusResponse>,
                response: Response<RecipeStatusResponse>
            ) {
                if (response.isSuccessful) {
                    putToastMessage("레시피가 업로드되었습니다.")
                    startHomeActivity(accessToken, refreshToken)
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의하세요.")
                }
            }

            override fun onFailure(call: Call<RecipeStatusResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }

        })
    }

    private fun patchRecipeData(accessToken: String, refreshToken: String, postBody: HashMap<String, Any>, recipeId: Int) {
        API.patchRecipe(accessToken, recipeId, postBody).enqueue(object: Callback<RecipeResponse>{
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful){
                    putToastMessage("레시피가 수정되었습니다.")
                    startHomeActivity(accessToken, refreshToken)
                }
                else {
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun startHomeActivity(accessToken: String, refreshToken: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("access_token", accessToken)
        intent.putExtra("refresh_token", refreshToken)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }

    private fun putToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}