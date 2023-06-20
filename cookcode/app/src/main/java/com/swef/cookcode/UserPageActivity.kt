package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.swef.cookcode.adapter.CreatedMembershipAdapter
import com.swef.cookcode.data.GlobalVariables.ERR_CODE
import com.swef.cookcode.data.GlobalVariables.accountAPI
import com.swef.cookcode.data.GlobalVariables.membershipAPI
import com.swef.cookcode.data.GlobalVariables.userId
import com.swef.cookcode.data.response.Membership
import com.swef.cookcode.data.response.MembershipResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.User
import com.swef.cookcode.data.response.UserResponse
import com.swef.cookcode.databinding.ActivityUserPageBinding
import com.swef.cookcode.databinding.MembershipSigninDialogBinding
import com.swef.cookcode.`interface`.MembershipSigninListener
import com.swef.cookcode.userinfofrags.UserCookieFragment
import com.swef.cookcode.userinfofrags.UserPremiumContentFragment
import com.swef.cookcode.userinfofrags.UserRecipeFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserPageActivity : AppCompatActivity(), MembershipSigninListener {

    private lateinit var binding : ActivityUserPageBinding

    private var madeUserId = ERR_CODE
    private val bundle = Bundle()
    private var subscribed = false
    private var isAdmin = false

    private lateinit var createdMembershipAdapter: CreatedMembershipAdapter
    private lateinit var membershipSigninDialogView: MembershipSigninDialogBinding
    private lateinit var membershipSigninDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.premiumContent.visibility = View.GONE

        madeUserId = intent.getIntExtra("user_id", ERR_CODE)
        bundle.putInt("user_id", madeUserId)

        if (intent.getStringExtra("is_admin") != null) {
            isAdmin = true
        }

        getInfoFromUserId()
        initContentView()

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        changeButtonSubscribed()

        binding.btnSubscribe.setOnClickListener {
            if (subscribed) {
                postSubscribe()
                subscribed = false
                changeButtonSubscribed()
            }
            else {
                postSubscribe()
                subscribed = true
                changeButtonSubscribed()
            }
        }

        createdMembershipAdapter = CreatedMembershipAdapter(this, this)
    }

    override fun onResume() {
        super.onResume()
        membershipSigninDialogView = MembershipSigninDialogBinding.inflate(layoutInflater)
        membershipSigninDialog = AlertDialog.Builder(this)
            .setView(membershipSigninDialogView.root)
            .create()

        createdMembershipAdapter = CreatedMembershipAdapter(this, this)
        membershipSigninDialogView.memberships.apply {
            adapter = createdMembershipAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        membershipSigninDialogView.btnCancel.setOnClickListener {
            membershipSigninDialog.dismiss()
        }

        binding.btnMembership.setOnClickListener {
            getCreatersMembership()
            membershipSigninDialog.show()
        }
    }

    private fun getCreatersMembership() {
        membershipAPI.getCreatersMemberships(madeUserId).enqueue(object : Callback<MembershipResponse> {
            override fun onResponse(
                call: Call<MembershipResponse>,
                response: Response<MembershipResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.membership.isNotEmpty()) {
                        val datas = response.body()!!.membership
                        createdMembershipAdapter.datas = datas as MutableList<Membership>
                        createdMembershipAdapter.notifyDataSetChanged()
                    }
                    else {
                        putToastMessage("아직 인플루언서의 멤버십이 존재하지 않습니다.")
                        membershipSigninDialog.dismiss()
                    }
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<MembershipResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
                Log.d("data_size", call.request().toString())
                Log.d("data_size", t.message.toString())
            }
        })
    }

    private fun changeButtonSubscribed() {
        if (subscribed) {
            binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component)
            binding.btnSubscribe.text = "구독중"
        }
        else {
            binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component_clicked)
            binding.btnSubscribe.text = "구독하기"
        }
    }

    private fun postSubscribe() {
        accountAPI.postUserSubscribe(madeUserId).enqueue(object : Callback<StatusResponse>{
            override fun onResponse(
                call: Call<StatusResponse>,
                response: Response<StatusResponse>
            ) {
                if (!response.isSuccessful){
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생!")
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun initContentView() {
        if (userId == madeUserId || isAdmin) {
            binding.btnSubscribe.visibility = View.GONE
        }

        val recipeFragment = UserRecipeFragment()
        val cookieFragment = UserCookieFragment()
        val premiumContentFragment = UserPremiumContentFragment()

        recipeFragment.arguments = bundle
        cookieFragment.arguments = bundle
        premiumContentFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, recipeFragment)
            .commitAllowingStateLoss()
        selectContentViewListener("recipe")

        binding.recipe.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, recipeFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("recipe")
        }

        binding.cookie.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, cookieFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("cookie")
        }

        binding.premiumContent.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, premiumContentFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("premium")
        }
    }

    private fun selectContentViewListener(content: String) {
        when(content) {
            "recipe" -> {
                binding.recipe.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.recipe.setBackgroundResource(R.drawable.under_bar_component_clicked)
                binding.cookie.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.cookie.setBackgroundResource(R.drawable.under_bar_component)
                binding.premiumContent.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.premiumContent.setBackgroundResource(R.drawable.under_bar_component)
            }
            "cookie" -> {
                binding.recipe.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.recipe.setBackgroundResource(R.drawable.under_bar_component)
                binding.cookie.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.cookie.setBackgroundResource(R.drawable.under_bar_component_clicked)
                binding.premiumContent.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.premiumContent.setBackgroundResource(R.drawable.under_bar_component)
            }
            "premium" -> {
                binding.recipe.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.recipe.setBackgroundResource(R.drawable.under_bar_component)
                binding.cookie.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.cookie.setBackgroundResource(R.drawable.under_bar_component)
                binding.premiumContent.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.premiumContent.setBackgroundResource(R.drawable.under_bar_component_clicked)
            }
        }
    }

    private fun getInfoFromUserId() {
        accountAPI.getUserInfo(madeUserId).enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val authority = response.body()!!.user.authority
                    authorityCheck(authority)
                    initUserInfo(response.body()!!.user)
                }
                else {
                    Log.d("data_size", call.request().toString())
                    Log.d("data_size", response.errorBody()!!.string())
                    putToastMessage("에러 발생! 관리자에게 문의해주세요.")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                putToastMessage("잠시 후 다시 시도해주세요.")
            }
        })
    }

    private fun authorityCheck(authority: String) {
        if (authority == "USER") {
            binding.btnMembership.visibility = View.GONE
        }
    }

    private fun initUserInfo(user: User) {
        binding.nicknameTitle.text = user.nickname
        binding.nickname.text = user.nickname
        binding.subscribedUsers.text = getString(R.string.subscribe_users, user.subscriberCount)

        initSubscribeButton(user.isSubscribed)
        if(user.profileImage != null) {
            getImageFromUrl(user.profileImage, binding.userProfile)
        }
    }

    private fun initSubscribeButton(isSubscribed: Boolean) {
        if (isSubscribed) {
            binding.btnSubscribe.setBackgroundResource(R.drawable.filled_fullround_component)
            binding.btnSubscribe.text = "구독중"
        }
    }

    private fun getImageFromUrl(imageUrl: String, view: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .into(view)
    }

    private fun putToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun signinSuccess() {
        membershipSigninDialog.dismiss()
    }
}