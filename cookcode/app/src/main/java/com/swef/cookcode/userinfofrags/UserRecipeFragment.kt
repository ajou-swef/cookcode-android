package com.swef.cookcode.userinfofrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swef.cookcode.UserPageActivity
import com.swef.cookcode.databinding.FragmentUserRecipeBinding

class UserRecipeFragment : Fragment() {

    private var _binding : FragmentUserRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private var userId = UserPageActivity.ERR_USER_CODE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}