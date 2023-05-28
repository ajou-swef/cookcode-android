package com.swef.cookcode.navifrags

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.CookieFormActivity
import com.swef.cookcode.MypageActivity
import com.swef.cookcode.R
import com.swef.cookcode.RecipeFormActivity
import com.swef.cookcode.SearchActivity
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.api.RecipeAPI
import com.swef.cookcode.data.RecipeAndStepData
import com.swef.cookcode.data.RecipeData
import com.swef.cookcode.data.StepData
import com.swef.cookcode.data.response.RecipeContent
import com.swef.cookcode.data.response.RecipeResponse
import com.swef.cookcode.data.response.UserResponse
import com.swef.cookcode.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {

    // fragment view binding을 위한 변수
    private var _binding : FragmentHomeBinding? = null
    // nullable할 경우 ?를 계속 붙여줘야 하기 때문에 non-null 타입으로 포장
    private val binding get() = _binding!!

    private val USER_ERR_CODE = -1

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = USER_ERR_CODE
    private lateinit var authority: String

    private var searchedRecipeAndStepDatas = mutableListOf<RecipeAndStepData>()

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    private var sort = "createdAt"
    private var createdMonth = 5
    private val pageSize = 10
    private var currentPage = 0

    private val TRUE = 1
    private val FALSE = 0

    private var cookable = 0

    private val recipeAPI = RecipeAPI.create()
    private val accountAPI = AccountAPI.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!
        userId = arguments?.getInt("user_id")!!
        authority = arguments?.getString("authority")!!

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
                getNewRecipeDatas()
            } else {
                cookable = FALSE
                getNewRecipeDatas()
            }
        }

        binding.btnSearch.setOnClickListener {
            val nextIntent = Intent(activity, SearchActivity::class.java)
            nextIntent.putExtra("access_token", accessToken)
            nextIntent.putExtra("refresh_token", refreshToken)
            nextIntent.putExtra("user_id", userId)
            startActivity(nextIntent)
        }

        binding.userMark.setOnClickListener {
            getUserData(accessToken, userId)
        }

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter(requireContext())
        recyclerViewAdapter.accessToken = accessToken
        recyclerViewAdapter.refreshToken = refreshToken
        recyclerViewAdapter.userId = userId

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = recyclerViewAdapter

        initOnScrollListener(linearLayoutManager)

        getNewRecipeDatas()

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
                R.id.cookie -> {
                    val nextIntent = Intent(activity, CookieFormActivity::class.java)
                    nextIntent.putExtra("access_token", accessToken)
                    nextIntent.putExtra("refresh_token", refreshToken)
                    nextIntent.putExtra("user_id", userId)
                    nextIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(nextIntent)
                    true
                }
                R.id.recipe -> {
                    val nextIntent = Intent(activity, RecipeFormActivity::class.java)
                    nextIntent.putExtra("access_token", accessToken)
                    nextIntent.putExtra("refresh_token", refreshToken)
                    nextIntent.putExtra("user_id", userId)
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
        recipeAPI.getRecipes(accessToken, currentPage, pageSize, cookable).enqueue(object :
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

    private fun getNewRecipeDatas() {
        currentPage = 0
        recipeAPI.getRecipes(accessToken, currentPage, pageSize, cookable).enqueue(object :
            Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                val datas = response.body()
                if (datas != null && datas.status == 200) {
                    searchedRecipeAndStepDatas = getRecipeDatasFromResponseBody(datas.recipes.content)
                    putNewDataForRecyclerview()
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
            val recipeData = RecipeData(
                item.recipeId, item.title, item.description,
                item.mainImage, item.likeCount, item.isCookable,
                item.user, item.createdAt.substring(0, 10), item.ingredients, item.additionalIngredients)
            val stepDatas = emptyList<StepData>()
            recipeAndStepDatas.add(RecipeAndStepData(recipeData, stepDatas))
        }

        return recipeAndStepDatas
    }

    private fun putDataForRecyclerview() {
        val isEmpty = recyclerViewAdapter.datas.isEmpty()

        if (isEmpty) {
            recyclerViewAdapter.datas = searchedRecipeAndStepDatas
            recyclerViewAdapter.notifyItemRangeInserted(0, recyclerViewAdapter.datas.size)
        }
        else {
            val beforeSize = recyclerViewAdapter.datas.size
            recyclerViewAdapter.datas.addAll(searchedRecipeAndStepDatas)
            recyclerViewAdapter.notifyItemRangeInserted(beforeSize, recyclerViewAdapter.datas.size)
        }
    }

    private fun putNewDataForRecyclerview() {
        recyclerViewAdapter.datas = searchedRecipeAndStepDatas
        recyclerViewAdapter.notifyDataSetChanged()
    }

    private fun getUserData(accessToken: String, userId: Int) {
        accountAPI.getUserInfo(accessToken, userId).enqueue(object: Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val nickname = response.body()!!.userData.nickname
                    startMyPageActivity(nickname)
                }
                else {
                    Log.d("data_size", response.errorBody()!!.string())
                    Log.d("data_size", call.request().toString())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun startMyPageActivity(nickname: String) {
        val nextIntent = Intent(activity, MypageActivity::class.java)
        nextIntent.putExtra("user_name", nickname)
        nextIntent.putExtra("access_token", accessToken)
        nextIntent.putExtra("refresh_token", refreshToken)
        nextIntent.putExtra("user_id", userId)
        nextIntent.flags = FLAG_ACTIVITY_CLEAR_TOP
        startActivity(nextIntent)
    }

    private fun initOnScrollListener(linearLayoutManager: LinearLayoutManager) {
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)

                if(differentY > 0) {
                    val visibleItemCount = linearLayoutManager.childCount
                    val totalItemCount = linearLayoutManager.itemCount
                    val pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        currentPage++
                        getRecipeDatas()
                    }
                }

                if(!recyclerView.canScrollVertically(-1)){
                    putToastMessage("데이터를 불러오는 중입니다.")
                    getNewRecipeDatas()
                }
            }
        })
    }
}