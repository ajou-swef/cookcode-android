package com.swef.cookcode.searchfrags

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.R
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.databinding.FragmentSearchRecipeBinding

class SearchRecipeFragment : Fragment() {

    private var _binding: FragmentSearchRecipeBinding? = null
    private val binding get() = _binding!!

    // 레시피 mock data
    private val recipeData = mutableListOf<RecipeData>()

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecipeBinding.inflate(inflater, container, false)

        // recipe에 들어가는 모든 사진은 drawable/foor_example로 대체
        val res: Resources = resources
        val uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + res.getResourcePackageName(R.drawable.food_example) +
                    '/' + res.getResourceTypeName(R.drawable.food_example) +
                    '/' + res.getResourceEntryName(R.drawable.food_example)
        )

        recipeData.apply {
            add(RecipeData("제육볶음", "맛있는 제육볶음", uri, 25, 25, "haeiny"))
            add(RecipeData("무말랭이", "맛있는 무말랭이", uri, 5, 10, "ymei"))
        }

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter()
        recyclerViewAdapter.datas = recipeData
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = recyclerViewAdapter

        recyclerViewAdapter.notifyDataSetChanged()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}