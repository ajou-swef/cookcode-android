package com.swef.cookcode.userinfofrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swef.cookcode.adapter.SearchUserRecyclerviewAdapter
import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.databinding.FragmentUserSubscriberBinding

class UserSubscriberFragment : Fragment() {

    companion object {
        const val ERR_USER_CODE = -1
    }

    private var _binding: FragmentUserSubscriberBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: SearchUserRecyclerviewAdapter

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = ERR_USER_CODE

    private var page = 0
    private val pageSize = 10
    private var hasNext = false

    private val API = AccountAPI.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserSubscriberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}