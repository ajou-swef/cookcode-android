package com.swef.cookcode.navifrags

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.swef.cookcode.R
import com.swef.cookcode.RecipeFormActivity
import com.swef.cookcode.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    // fragment view binding을 위한 변수
    private var _binding : FragmentHomeBinding? = null
    // nullable할 경우 ?를 계속 붙여줘야 하기 때문에 non-null 타입으로 포장
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 컨텐츠 추가 버튼 click listener
        binding.btnAddContents.setOnClickListener{
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
                        nextIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(nextIntent)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
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
}