package com.swef.cookcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.swef.cookcode.data.GlobalVariables.ERR_CODE
import com.swef.cookcode.databinding.ActivitySubscriberBinding
import com.swef.cookcode.subscribefrags.SubscribedFragment
import com.swef.cookcode.subscribefrags.SubscriberFragment

class SubscriberActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySubscriberBinding

    private var userId = ERR_CODE

    private val bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("user_id", ERR_CODE)
        bundle.putInt("user_id", userId)

        binding.beforeArrow.setOnClickListener {
            finish()
        }

        initContentView()
    }

    private fun initContentView() {
        val subscribedFragment = SubscribedFragment()
        val subscriberFragment = SubscriberFragment()

        subscribedFragment.arguments = bundle
        subscriberFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, subscribedFragment)
            .commitAllowingStateLoss()
        selectContentViewListener("Subscribed")

        binding.btnSubscribed.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, subscribedFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("Subscribed")
        }

        binding.btnSubscribers.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_container, subscriberFragment)
                .commitAllowingStateLoss()
            selectContentViewListener("Subscriber")
        }
    }

    private fun selectContentViewListener(content: String) {
        when (content) {
            "Subscribed" -> {
                binding.btnSubscribed.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.btnSubscribed.setBackgroundResource(R.drawable.under_bar_component_clicked)
                binding.btnSubscribers.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.btnSubscribers.setBackgroundResource(R.drawable.under_bar_component)
            }
            "Subscriber" -> {
                binding.btnSubscribed.setTextColor(ContextCompat.getColor(this, R.color.gray_80))
                binding.btnSubscribed.setBackgroundResource(R.drawable.under_bar_component)
                binding.btnSubscribers.setTextColor(ContextCompat.getColor(this, R.color.main_theme))
                binding.btnSubscribers.setBackgroundResource(R.drawable.under_bar_component_clicked)
            }
        }
    }
}