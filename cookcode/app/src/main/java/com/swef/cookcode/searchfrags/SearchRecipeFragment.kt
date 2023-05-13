package com.swef.cookcode.searchfrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.response.Photos
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeResponse
import com.swef.cookcode.data.response.Step
import com.swef.cookcode.data.response.Videos
import com.swef.cookcode.databinding.FragmentSearchRecipeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRecipeFragment : Fragment() {

    private var _binding: FragmentSearchRecipeBinding? = null
    private val binding get() = _binding!!

    // 레시피 mock data
    private var searchedRecipeAndStepDatas = mutableListOf<RecipeAndStepData>()

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    private var cookable = 0
    private var sort = "createAt"
    private var createdMonth = 5
    private val pageSize = 20

    private val API = RecipeAPI.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeBinding.inflate(inflater, container, false)

        val accessToken = arguments?.getString("access_token")!!
        val searchKeyword = arguments?.getString("keyword")!!

        var currentPage = 0

        Thread {
            searchedRecipeAndStepDatas = getRecipeDatas(accessToken, currentPage, pageSize, cookable, sort, createdMonth)
        }.start()
        Thread.sleep(100)

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter()
        recyclerViewAdapter.datas = searchedRecipeAndStepDatas
        recyclerViewAdapter.accessToken = accessToken
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = recyclerViewAdapter

        recyclerViewAdapter.notifyDataSetChanged()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getRecipeDatas(accessToken: String, currentPage: Int, sizePerPage: Int, cookable: Int, sort: String, createdMonth: Int): MutableList<RecipeAndStepData> {
        var recipeAndStepDatas = mutableListOf<RecipeAndStepData>()

        Log.d("data_size", accessToken)

        API.getRecipes(accessToken, currentPage, sizePerPage, sort, createdMonth, cookable).enqueue(object: Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                val datas = response.body()
                if (datas != null && datas.status == 200) {
                    recipeAndStepDatas = getRecipeDatasFromResponseBody(datas.recipes)
                }

                if (response.errorBody() != null) {
                    Log.d("data_size", response.errorBody()!!.string())
                }
                else {
                    Log.d("data_size", response.toString())
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }

        })

        return recipeAndStepDatas
    }

    private fun getRecipeDatasFromResponseBody(datas: List<RecipeContent>): MutableList<RecipeAndStepData> {
        val recipeAndStepDatas = mutableListOf<RecipeAndStepData>()

        for (item in datas) {
            val recipeData = RecipeData(item.recipeId, item.title, item.description, item.mainImage.toUri(), item.likeCount, item.user)
            val stepDatas = getStepDatasFromRecipeContent(item.steps)

            recipeAndStepDatas.add(RecipeAndStepData(recipeData, stepDatas))
        }

        return recipeAndStepDatas
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
}