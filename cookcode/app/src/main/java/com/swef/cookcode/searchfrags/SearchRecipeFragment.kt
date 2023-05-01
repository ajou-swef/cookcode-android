package com.swef.cookcode.searchfrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.swef.cookcode.R
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.databinding.FragmentSearchRecipeBinding

class SearchRecipeFragment : Fragment() {

    private var _binding: FragmentSearchRecipeBinding? = null
    private val binding get() = _binding!!

    // 레시피 mock data
    private val recipeData = mutableListOf<RecipeData>()
    private val stepDatas = mutableListOf<StepData>()
    private val exampleImageUri = "drawable://" + R.drawable.food_example

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeBinding.inflate(inflater, container, false)
        stepDatas.apply {
            for(i: Int in 0..2) {
                val imageDatas = mutableListOf<String>()
                imageDatas.apply {
                    add(exampleImageUri)
                    add(exampleImageUri)
                    add(exampleImageUri)
                }
                add(StepData(imageDatas, null, i.toString() + "단계", i.toString() + "단계 요리 만들기", i + 1))
            }
        }
        recipeData.apply {
            add(RecipeData(stepDatas, "제육볶음", "맛있는 제육볶음", exampleImageUri.toUri(), 25, 25, "haeiny"))
            add(RecipeData(stepDatas, "무말랭이", "맛있는 무말랭이", exampleImageUri.toUri(), 5, 10, "ymei"))
        }

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter()
        recyclerViewAdapter.datas = recipeData
        binding.recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter.notifyDataSetChanged()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}