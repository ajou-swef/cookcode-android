package com.swef.cookcode.searchfrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private var searchedRecipeAndStepDatas = mutableListOf<RecipeAndStepData>()

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    private var cookable = 1
    private var sort = "createdAt"
    private var createdMonth = 5
    private val pageSize = 10

    private val API = RecipeAPI.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeBinding.inflate(inflater, container, false)

        val accessToken = arguments?.getString("access_token")!!
        val searchKeyword = arguments?.getString("keyword")!!

        var currentPage = 0

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter(requireContext())

        recyclerViewAdapter.accessToken = accessToken
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = recyclerViewAdapter

        // searchedRecipeAndStepDatas = getRecipeDatas(accessToken, currentPage, pageSize, cookable, sort, createdMonth)

        API.getRecipes(accessToken, currentPage, pageSize, cookable).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                val datas = response.body()
                if (datas != null && datas.status == 200) {
                    Log.d("data_size", response.body().toString())
                    searchedRecipeAndStepDatas = getRecipeDatasFromResponseBody(datas.recipes.content)
                    putDataForRecyclerview()
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getRecipeDatasFromResponseBody(datas: List<RecipeContent>): MutableList<RecipeAndStepData> {
        val recipeAndStepDatas = mutableListOf<RecipeAndStepData>()

        for (item in datas) {
            val recipeData = RecipeData(
                item.recipeId, item.title, item.description,
                item.mainImage, item.likeCount, item.isCookable,
                item.user, item.createdAt, item.ingredients, item.additionalIngredients)
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

    private fun putDataForRecyclerview() {
        recyclerViewAdapter.datas = searchedRecipeAndStepDatas
        recyclerViewAdapter.notifyItemRangeInserted(0, recyclerViewAdapter.datas.size - 1)
    }
}