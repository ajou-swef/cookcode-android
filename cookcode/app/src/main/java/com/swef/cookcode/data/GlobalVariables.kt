package com.swef.cookcode.data

import com.swef.cookcode.api.AccountAPI
import com.swef.cookcode.api.AdminAPI
import com.swef.cookcode.api.AuthService
import com.swef.cookcode.api.CookieAPI
import com.swef.cookcode.api.FridgeAPI
import com.swef.cookcode.api.MembershipAPI
import com.swef.cookcode.api.RecipeAPI

object GlobalVariables {
    var accessToken: String = ""
    var refreshToken: String = ""
    var userId: Int = -1
    var authority: String = ""

    val accountAPI = AccountAPI.create()
    val recipeAPI = RecipeAPI.create()
    val cookieAPI = CookieAPI.create()
    val fridgeAPI = FridgeAPI.create()
    val adminAPI = AdminAPI.create()
    val authService = AuthService.create()
    val membershipAPI = MembershipAPI.create()

    const val ERR_CODE = -1
    const val TRUE = 1
    const val FALSE = 0
    const val SPAN_COUNT = 3
}