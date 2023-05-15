package com.swef.cookcode.navifrags

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.swef.cookcode.MypageActivity
import com.swef.cookcode.R
import com.swef.cookcode.RecipeFormActivity
import com.swef.cookcode.SearchActivity
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
import com.swef.cookcode.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {

    // fragment view binding을 위한 변수
    private var _binding : FragmentHomeBinding? = null
    // nullable할 경우 ?를 계속 붙여줘야 하기 때문에 non-null 타입으로 포장
    private val binding get() = _binding!!

    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    private var searchedRecipeAndStepDatas = mutableListOf<RecipeAndStepData>()

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    private var sort = "createdAt"
    private var createdMonth = 5
    private val pageSize = 10
    private var currentPage = 0

    private val TRUE = 1
    private val FALSE = 0

    private var cookable = 0

    private val API = RecipeAPI.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!

        // 컨텐츠 추가 버튼 click listener
        binding.btnAddContents.setOnClickListener{
            showPopupMenuToCreateContent()
        }
        // recyclerview에 가려지므로 최상단으로 버튼을 올려줌
        binding.btnAddContents.bringToFront()

        binding.btnSort.setOnClickListener {
            showPopupMenuToSetSort()
        }

        binding.btnCookable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cookable = TRUE
                getRecipeDatas()
            } else {
                cookable = FALSE
                getRecipeDatas()
            }
        }

        binding.btnSearch.setOnClickListener {
            val nextIntent = Intent(activity, SearchActivity::class.java)
            nextIntent.putExtra("access_token", accessToken)
            startActivity(nextIntent)
        }

        binding.userMark.setOnClickListener {
            val nextIntent = Intent(activity, MypageActivity::class.java)
            nextIntent.putExtra("user_name", "쿡코드")
            nextIntent.putExtra("access_token", accessToken)
            nextIntent.flags = FLAG_ACTIVITY_CLEAR_TOP
            startActivity(nextIntent)
        }

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter()

        recyclerViewAdapter.accessToken = accessToken
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = recyclerViewAdapter

        getRecipeDatas()

        return binding.root
    }

    // Fragment는 생명 주기가 매우 길기 때문에 view가 destroy되어도 fragment는 살아있음
    // 따라서 메모리 누수가 발생하기 때문에 view가 죽을 시 binding을 null로 설정해줌
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 컨텐츠 추가 버튼 클릭시 나타날 팝업 메뉴 항목 정의
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.content_popup_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showPopupMenuToCreateContent(){
        // fragment가 연결된 context를 불러와 해당 버튼에 팝업 메뉴가 나타나도록 함
        val popupMenu = PopupMenu(requireContext(), binding.btnAddContents)
        // icon 보여주기
        popupMenu.setForceShowIcon(true)
        // 메뉴 항목을 inflate
        popupMenu.menuInflater.inflate(R.menu.content_popup_menu, popupMenu.menu)

        // 팝업 메뉴 아이템 클릭 리스너
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cookie -> true
                R.id.recipe -> {
                    val nextIntent = Intent(activity, RecipeFormActivity::class.java)
                    nextIntent.putExtra("access_token", accessToken)
                    nextIntent.putExtra("refresh_token", refreshToken)
                    nextIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(nextIntent)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showPopupMenuToSetSort() {
        val popupMenu = PopupMenu(requireContext(), binding.btnSort)
        popupMenu.menuInflater.inflate(R.menu.sort_popup_menu, popupMenu.menu)

        // 팝업 메뉴 아이템 클릭 리스너
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.createdAt -> {
                    sort = "createdAt"
                    getRecipeDatas()
                    true
                }
                R.id.popular -> {
                    // 아직 popular는 구현되지 않음
                    // sort = "popular"
                    getRecipeDatas()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getRecipeDatas() {
        API.getRecipes(accessToken, currentPage, pageSize).enqueue(object :
            Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                val datas = response.body()
                if (datas != null && datas.status == 200) {
                    searchedRecipeAndStepDatas = getRecipeDatasFromResponseBody(datas.recipes.content)
                    putDataForRecyclerview()
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
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

        // mock data
        stepDatas.apply {
            val imageUri = mutableListOf<String>()
            imageUri.add("https://picsum.photos/200")
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

    private fun putDataForRecyclerview() {
        val isEmpty = recyclerViewAdapter.datas.isEmpty()

        recyclerViewAdapter.datas = searchedRecipeAndStepDatas

        if (isEmpty) {
            recyclerViewAdapter.notifyItemRangeInserted(0, recyclerViewAdapter.datas.size - 1)
        }
        else {
            recyclerViewAdapter.notifyItemRangeChanged(0, recyclerViewAdapter.datas.size - 1)
        }
    }
}