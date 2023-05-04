package com.swef.cookcode.data.host

import android.net.Uri
import com.swef.cookcode.R
import com.swef.cookcode.data.IngredientData

// 식재료들은 백엔드에서 구현된 목록으로만 존재함
// 따라서 식재료 아이템을 가져오는 함수를 미리 구현해두어 불러오기만 하면 되도록 했음
class IngredientDataHost {

    private val datas = showAllIngredientData()

    // 식재료 등록을 위한 모든 식재료 정보 불러오기
    fun showAllIngredientData(): List<IngredientData>{
        val datas = mutableListOf<IngredientData>()

        var uri = getUriForResource(R.drawable.icon_meat)
        datas.apply {
            add(IngredientData(uri, "삼겹살", null, "meat", 1))
            add(IngredientData(uri, "돼지 목살", null, "meat", 2))
            add(IngredientData(uri, "돼지 갈비", null, "meat", 3))
            add(IngredientData(uri, "닭 한마리", null, "meat", 4))
            add(IngredientData(uri, "닭가슴살", null, "meat", 5))
            add(IngredientData(uri, "소고기 등심", null, "meat", 6))
            add(IngredientData(uri, "소고기 안심", null, "meat", 7))
            add(IngredientData(uri, "소 갈비", null, "meat", 8))
            add(IngredientData(uri, "베이컨", null, "meat", 9))
            add(IngredientData(uri, "소시지", null, "meat", 10))
            add(IngredientData(uri, "계란", null, "meat", 11))
        }

        uri = getUriForResource(R.drawable.icon_seafood)
        datas.apply {
            add(IngredientData(uri, "연어", null, "seafood", 12))
            add(IngredientData(uri, "고등어", null, "seafood", 13))
            add(IngredientData(uri, "갈치", null, "seafood", 14))
            add(IngredientData(uri, "참치", null, "seafood", 15))
            add(IngredientData(uri, "멸치", null, "seafood", 16))
            add(IngredientData(uri, "새우", null, "seafood", 17))
            add(IngredientData(uri, "굴", null, "seafood", 18))
            add(IngredientData(uri, "오징어", null, "seafood", 19))
            add(IngredientData(uri, "문어", null, "seafood", 20))
            add(IngredientData(uri, "미역", null, "seafood", 21))
        }

        uri = getUriForResource(R.drawable.icon_grain)
        datas.apply {
            add(IngredientData(uri, "쌀", null, "grain", 22))
            add(IngredientData(uri, "보리", null, "grain", 23))
            add(IngredientData(uri, "밀가루", null, "grain", 24))
            add(IngredientData(uri, "옥수수", null, "grain", 25))
            add(IngredientData(uri, "콩", null, "grain", 26))
            add(IngredientData(uri, "면", null, "grain", 27))
        }

        uri = getUriForResource(R.drawable.icon_vegetable)
        datas.apply {
            add(IngredientData(uri, "양파", null, "vegetable", 28))
            add(IngredientData(uri, "마늘", null, "vegetable", 29))
            add(IngredientData(uri, "당근", null, "vegetable", 30))
            add(IngredientData(uri, "오이", null, "vegetable", 31))
            add(IngredientData(uri, "배추", null, "vegetable", 32))
            add(IngredientData(uri, "파", null, "vegetable", 33))
            add(IngredientData(uri, "고추", null, "vegetable", 34))
            add(IngredientData(uri, "깻잎", null, "vegetable", 35))
            add(IngredientData(uri, "상추", null, "vegetable", 36))
            add(IngredientData(uri, "콩나물", null, "vegetable", 37))
            add(IngredientData(uri, "버섯", null, "vegetable", 38))
            add(IngredientData(uri, "무", null, "vegetable", 39))
            add(IngredientData(uri, "브로콜리", null, "vegetable", 40))
            add(IngredientData(uri, "피망", null, "vegetable", 41))
            add(IngredientData(uri, "감자", null, "vegetable", 42))
        }

        uri = getUriForResource(R.drawable.icon_fruit)
        datas.apply {
            add(IngredientData(uri, "사과", null, "fruit", 43))
            add(IngredientData(uri, "배", null, "fruit", 44))
            add(IngredientData(uri, "바나나", null, "fruit", 45))
            add(IngredientData(uri, "토마토", null, "fruit", 46))
        }

        uri = getUriForResource(R.drawable.icon_sauce)
        datas.apply {
            add(IngredientData(uri, "간장", null, "sauce", 47))
            add(IngredientData(uri, "고추장", null, "sauce", 48))
            add(IngredientData(uri, "된장", null, "sauce", 49))
            add(IngredientData(uri, "소금", null, "sauce", 50))
            add(IngredientData(uri, "설탕", null, "sauce", 51))
            add(IngredientData(uri, "토마토 소스", null, "sauce", 52))
            add(IngredientData(uri, "식초", null, "sauce", 53))
            add(IngredientData(uri, "참기름", null, "sauce", 54))
            add(IngredientData(uri, "들기름", null, "sauce", 55))
            add(IngredientData(uri, "초고추장", null, "sauce", 56))
            add(IngredientData(uri, "물엿", null, "sauce", 57))
            add(IngredientData(uri, "고춧가루", null, "sauce", 58))
            add(IngredientData(uri, "돈까스 소스", null, "sauce", 59))
            add(IngredientData(uri, "케찹", null, "sauce", 60))
            add(IngredientData(uri, "머스타드", null, "sauce", 61))
            add(IngredientData(uri, "후추", null, "sauce", 62))
        }

        return datas
    }

    fun getIngredientFromId(id: Int): IngredientData? {
        return datas.find { it.ingredId == id }
    }

    fun getIngredientFromNameOrType(keyword: String): List<IngredientData> {
        return datas.filter { it.name.contains(keyword) }
    }

    private fun getUriForResource(resId: Int): Uri{
        return Uri.parse("android.resource://com.swef.cookcode/" + resId)
    }
}