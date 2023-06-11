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
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swef.cookcode.CookieFormActivity
import com.swef.cookcode.MypageActivity
import com.swef.cookcode.R
import com.swef.cookcode.RecipeFormActivity
import com.swef.cookcode.SearchActivity
import com.swef.cookcode.adapter.SearchRecipeRecyclerviewAdapter
import com.swef.cookcode.data.GlobalVariables.FALSE
import com.swef.cookcode.data.GlobalVariables.TRUE
import com.swef.cookcode.data.GlobalVariables.accountAPI
import com.swef.cookcode.data.GlobalVariables.recipeAPI
import com.swef.cookcode.data.GlobalVariables.userId
import com.swef.cookcode.data.SearchedRecipeData
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

    private var searchedRecipeDatas = mutableListOf<SearchedRecipeData>()

    private lateinit var recyclerViewAdapter: SearchRecipeRecyclerviewAdapter

    private var sort = "createdAt"
    private var createdMonth = 5
    private val pageSize = 5
    private var currentPage = 0

    private var cookable = 0

    private var hasNext = false
    private var isScrollingUp = false
    private var isScrollingDown = false

    private lateinit var nickname: String
    private var profileImage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        getUserData(userId)

        // 컨텐츠 추가 버튼 click listener
        binding.btnAddContents.setOnClickListener{
            showPopupMenuToCreateContent()
        }
        // recyclerview에 가려지므로 최상단으로 버튼을 올려줌
        binding.btnAddContents.bringToFront()

        binding.btnCreatedAt.setOnClickListener {
            sort = "createdAt"
            getNewRecipeDatas()
            changeButtonBackground("createdAt")
        }

        binding.btnPopular.setOnClickListener {
            sort = "popular"
            getNewRecipeDatas()
            changeButtonBackground("popular")
        }

        binding.btnSubscribed.setOnClickListener {
            // sort = "subscribed"
            getNewRecipeDatas()
            changeButtonBackground("subscribed")
        }

        binding.btnMembership.setOnClickListener {
            // sort = "membership"
            getNewRecipeDatas()
            changeButtonBackground("membership")
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
            startActivity(nextIntent)
        }

        binding.userMark.setOnClickListener {
            startMyPageActivity(nickname, profileImage)
        }

        recyclerViewAdapter = SearchRecipeRecyclerviewAdapter(requireContext())

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = recyclerViewAdapter

        getNewRecipeDatas()
        initOnScrollListener()

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
                    nextIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(nextIntent)
                    true
                }
                R.id.recipe -> {
                    val nextIntent = Intent(activity, RecipeFormActivity::class.java)
                    nextIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(nextIntent)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun changeButtonBackground(type: String) {
        when (type) {
            "createdAt" -> {
                binding.btnCreatedAt.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.btnCreatedAt.setBackgroundResource(R.drawable.fullround_component_no_padding)
                binding.btnPopular.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnPopular.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnSubscribed.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnSubscribed.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnMembership.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnMembership.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
            }
            "popular" -> {
                binding.btnCreatedAt.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnCreatedAt.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnPopular.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.btnPopular.setBackgroundResource(R.drawable.fullround_component_no_padding)
                binding.btnSubscribed.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnSubscribed.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnMembership.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnMembership.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
            }
            "subscribed" -> {
                binding.btnCreatedAt.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnCreatedAt.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnPopular.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnPopular.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnSubscribed.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.btnSubscribed.setBackgroundResource(R.drawable.fullround_component_no_padding)
                binding.btnMembership.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnMembership.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
            }
            "membership" -> {
                binding.btnCreatedAt.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnCreatedAt.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnPopular.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnPopular.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnSubscribed.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_80))
                binding.btnSubscribed.setBackgroundResource(R.drawable.fullround_component_no_padding_gray)
                binding.btnMembership.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.btnMembership.setBackgroundResource(R.drawable.fullround_component_no_padding)
            }
        }
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getRecipeDatas() {
        recipeAPI.getRecipes(currentPage, pageSize, sort, cookable).enqueue(object :
            Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
                    val datas = response.body()!!
                    searchedRecipeDatas = getRecipeDatasFromResponseBody(datas.recipes.content)
                    hasNext = datas.recipes.hasNext
                    putDataForRecyclerview()
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getNewRecipeDatas() {
        currentPage = 0
        recipeAPI.getRecipes(currentPage, pageSize, sort, cookable).enqueue(object :
            Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                putToastMessage("데이터를 불러오고 있습니다.")
                if (response.isSuccessful) {
                    val datas = response.body()!!
                    searchedRecipeDatas = getRecipeDatasFromResponseBody(datas.recipes.content)
                    hasNext = datas.recipes.hasNext
                    putNewDataForRecyclerview()
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun getRecipeDatasFromResponseBody(datas: List<RecipeContent>): MutableList<SearchedRecipeData> {
        val recipeDatas = mutableListOf<SearchedRecipeData>()

        for (item in datas) {
            val recipeData = SearchedRecipeData(
                item.recipeId, item.title, item.description,
                item.mainImage, item.likeCount, item.isLiked, item.isCookable,
                item.user, item.createdAt.substring(0, 10))
            recipeDatas.add(recipeData)
        }

        return recipeDatas
    }

    private fun putDataForRecyclerview() {
        val isEmpty = recyclerViewAdapter.datas.isEmpty()

        if (isEmpty) {
            recyclerViewAdapter.datas = searchedRecipeDatas
            recyclerViewAdapter.notifyItemRangeInserted(0, recyclerViewAdapter.datas.size)
        }
        else {
            val beforeSize = recyclerViewAdapter.datas.size
            recyclerViewAdapter.datas.addAll(searchedRecipeDatas)
            recyclerViewAdapter.notifyItemRangeInserted(beforeSize, recyclerViewAdapter.datas.size)
        }
    }

    private fun putNewDataForRecyclerview() {
        recyclerViewAdapter.datas = searchedRecipeDatas
        recyclerViewAdapter.notifyDataSetChanged()
    }

    private fun getUserData(userId: Int) {
        accountAPI.getUserInfo(userId).enqueue(object: Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    nickname = response.body()!!.user.nickname
                    profileImage = response.body()!!.user.profileImage
                    if (profileImage != null) {
                        getImageFromUrl(profileImage!!, binding.userMark)
                    }
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

    private fun startMyPageActivity(nickname: String, profileImage: String?) {
        val nextIntent = Intent(activity, MypageActivity::class.java)
        nextIntent.putExtra("user_name", nickname)
        nextIntent.putExtra("profile_image", profileImage)
        nextIntent.flags = FLAG_ACTIVITY_CLEAR_TOP
        startActivity(nextIntent)
    }

    private fun initOnScrollListener() {
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isScrollingUp = false
                    isScrollingDown = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)
                isScrollingUp = differentY > 0
                isScrollingDown = differentY < 0

                if (isScrollingUp) {
                    if (!recyclerView.canScrollVertically(1) && hasNext) {
                        currentPage++
                        getRecipeDatas()
                    }
                }
                else if (isScrollingDown) {
                    if (!recyclerView.canScrollVertically(-1)) {
                        getNewRecipeDatas()
                    }
                }
            }
        })
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .into(view)
    }
}