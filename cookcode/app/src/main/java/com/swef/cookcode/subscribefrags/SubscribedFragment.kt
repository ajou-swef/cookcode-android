package com.swef.cookcode.subscribefrags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swef.cookcode.adapter.SearchUserRecyclerviewAdapter
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.data.UserData
import com.swef.cookcode.data.response.User
import com.swef.cookcode.data.response.UsersResponse
import com.swef.cookcode.databinding.FragmentSubscribedBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubscribedFragment : Fragment() {

    companion object {
        const val ERR_USER_CODE = -1
    }

    private var _binding: FragmentSubscribedBinding? = null
    private val binding get() = _binding!!

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = ERR_USER_CODE

    private var hasNext = false

    private val pageSize = 10
    private var page = 0

    private val API = AccountAPI.create()

    private lateinit var recyclerViewAdapter: SearchUserRecyclerviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscribedBinding.inflate(inflater, container, false)

        accessToken = arguments?.getString("access_token")!!
        refreshToken = arguments?.getString("refresh_token")!!
        userId = arguments?.getInt("user_id")!!

        recyclerViewAdapter = SearchUserRecyclerviewAdapter(requireContext())
        recyclerViewAdapter.accessToken = accessToken
        recyclerViewAdapter.refreshToken = refreshToken
        recyclerViewAdapter.userId = userId

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = recyclerViewAdapter
        }

        getNewMyPublishers()
        initOnScrollListener()

        return binding.root
    }

    private fun getNewMyPublishers() {
        page = 0
        recyclerViewAdapter.datas.clear()
        getMyPublishers()
    }

    private fun getMyPublishers() {
        API.getMyPublishers(accessToken).enqueue(object : Callback<UsersResponse> {
            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                if (response.isSuccessful){
                    val data = response.body()!!.users
                    val userDatas = getUserDatasFromResponseBody(data)

                    if (recyclerViewAdapter.datas.isEmpty()) {
                        recyclerViewAdapter.datas = userDatas as MutableList<UserData>
                        recyclerViewAdapter.notifyItemRangeChanged(0, userDatas.size)
                    }
                    else {
                        val beforeSize = recyclerViewAdapter.itemCount
                        recyclerViewAdapter.datas.addAll(userDatas)
                        recyclerViewAdapter.notifyItemRangeChanged(beforeSize, recyclerViewAdapter.itemCount)
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun initOnScrollListener() {
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, differentX: Int, differentY: Int) {
                super.onScrolled(recyclerView, differentX, differentY)

                if (recyclerViewAdapter.datas.isNotEmpty()) {
                    if (!recyclerView.canScrollVertically(1) && hasNext) {
                        page++
                        getMyPublishers()
                    } else if (!recyclerView.canScrollVertically(-1)) {
                        putToastMessage("데이터를 불러오는 중입니다.")
                        getNewMyPublishers()
                    }
                }
            }
        })
    }


    private fun getUserDatasFromResponseBody(data: List<User>): List<UserData> {
        val userDatas = mutableListOf<UserData>()

        for (item in data) {
            val nickname = item.nickname
            val userId = item.userId
            val profileImage = item.profileImage
            val subscribed = false

            userDatas.add(UserData(userId, nickname, profileImage, subscribed))
        }

        return userDatas
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun putToastMessage(message: String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }
}