package com.swef.cookcode.data.host

import android.net.Uri
import com.swef.cookcode.R
import com.swef.cookcode.data.IngredientData
import com.swef.cookcode.data.MyIngredientData

// 식재료들은 백엔드에서 구현된 목록으로만 존재함
// 따라서 식재료 아이템을 가져오는 함수를 미리 구현해두어 불러오기만 하면 되도록 했음
class IngredientDataHost {

    val datas = showAllIngredientData()

    // 식재료 등록을 위한 모든 식재료 정보 불러오기
    fun showAllIngredientData(): List<MyIngredientData>{
        val ingredDatas = mutableListOf<IngredientData>()
        val allData = mutableListOf<MyIngredientData>()

        var uri = getUriForResource(R.drawable.icon_meat)
        ingredDatas.apply {
            add(IngredientData(uri, "삼겹살", "meat", 1))
            add(IngredientData(uri, "돼지 목살", "meat", 2))
            add(IngredientData(uri, "돼지 갈비", "meat", 3))
            add(IngredientData(uri, "닭 한마리", "meat", 4))
            add(IngredientData(uri, "닭가슴살", "meat", 5))
            add(IngredientData(uri, "소고기 등심", "meat", 6))
            add(IngredientData(uri, "소고기 안심", "meat", 7))
            add(IngredientData(uri, "소 갈비", "meat", 8))
            add(IngredientData(uri, "베이컨", "meat", 9))
            add(IngredientData(uri, "소시지", "meat", 10))
            add(IngredientData(uri, "계란", "meat", 11))
        }

        uri = getUriForResource(R.drawable.icon_seafood)
        ingredDatas.apply {
            add(IngredientData(uri, "연어", "seafood", 12))
            add(IngredientData(uri, "고등어", "seafood", 13))
            add(IngredientData(uri, "갈치", "seafood", 14))
            add(IngredientData(uri, "참치", "seafood", 15))
            add(IngredientData(uri, "멸치", "seafood", 16))
            add(IngredientData(uri, "새우", "seafood", 17))
            add(IngredientData(uri, "굴", "seafood", 18))
            add(IngredientData(uri, "오징어", "seafood", 19))
            add(IngredientData(uri, "문어", "seafood", 20))
            add(IngredientData(uri, "미역", "seafood", 21))
        }

        uri = getUriForResource(R.drawable.icon_grain)
        ingredDatas.apply {
            add(IngredientData(uri, "쌀", "grain", 22))
            add(IngredientData(uri, "보리", "grain", 23))
            add(IngredientData(uri, "밀가루", "grain", 24))
            add(IngredientData(uri, "옥수수", "grain", 25))
            add(IngredientData(uri, "콩", "grain", 26))
            add(IngredientData(uri, "면", "grain", 27))
        }

        uri = getUriForResource(R.drawable.icon_vegetable)
        ingredDatas.apply {
            add(IngredientData(uri, "양파", "vegetable", 28))
            add(IngredientData(uri, "마늘", "vegetable", 29))
            add(IngredientData(uri, "당근", "vegetable", 30))
            add(IngredientData(uri, "오이", "vegetable", 31))
            add(IngredientData(uri, "배추", "vegetable", 32))
            add(IngredientData(uri, "파", "vegetable", 33))
            add(IngredientData(uri, "고추", "vegetable", 34))
            add(IngredientData(uri, "깻잎", "vegetable", 35))
            add(IngredientData(uri, "상추", "vegetable", 36))
            add(IngredientData(uri, "콩나물", "vegetable", 37))
            add(IngredientData(uri, "버섯", "vegetable", 38))
            add(IngredientData(uri, "무", "vegetable", 39))
            add(IngredientData(uri, "브로콜리", "vegetable", 40))
            add(IngredientData(uri, "피망", "vegetable", 41))
            add(IngredientData(uri, "감자", "vegetable", 42))
        }

        uri = getUriForResource(R.drawable.icon_fruit)
        ingredDatas.apply {
            add(IngredientData(uri, "사과", "fruit", 43))
            add(IngredientData(uri, "배", "fruit", 44))
            add(IngredientData(uri, "바나나", "fruit", 45))
            add(IngredientData(uri, "토마토", "fruit", 46))
        }

        uri = getUriForResource(R.drawable.icon_sauce)
        ingredDatas.apply {
            add(IngredientData(uri, "간장", "sauce", 47))
            add(IngredientData(uri, "고추장", "sauce", 48))
            add(IngredientData(uri, "된장", "sauce", 49))
            add(IngredientData(uri, "소금", "sauce", 50))
            add(IngredientData(uri, "설탕", "sauce", 51))
            add(IngredientData(uri, "토마토 소스", "sauce", 52))
            add(IngredientData(uri, "식초", "sauce", 53))
            add(IngredientData(uri, "참기름", "sauce", 54))
            add(IngredientData(uri, "들기름", "sauce", 55))
            add(IngredientData(uri, "초고추장", "sauce", 56))
            add(IngredientData(uri, "물엿", "sauce", 57))
            add(IngredientData(uri, "고춧가루", "sauce", 58))
            add(IngredientData(uri, "돈까스 소스", "sauce", 59))
            add(IngredientData(uri, "케찹", "sauce", 60))
            add(IngredientData(uri, "머스타드", "sauce", 61))
            add(IngredientData(uri, "후추", "sauce", 62))
        }

        for(item in ingredDatas){
            allData.apply {
                add(MyIngredientData(item, null, null, null, null))
            }
        }

        return allData
    }

    fun getIngredientFromId(id: Int): MyIngredientData? {
        return datas.find { it.ingredientData.ingredId == id }
    }

    fun getIngredientFromNameOrType(data:List<MyIngredientData>, keyword: String): List<MyIngredientData> {
        return data.filter { it.ingredientData.name.contains(keyword) }
    }

    private fun getUriForResource(resId: Int): Uri{
        return Uri.parse("android.resource://com.swef.cookcode/" + resId)
    }

    fun removeElement(data: List<MyIngredientData>, removeDatas: List<MyIngredientData>): MutableList<MyIngredientData> {
        val returnData = mutableListOf<MyIngredientData>()
        returnData.addAll(data)

        for (item in data) {
            for (removeItem in removeDatas){
                if (item.ingredientData.name == removeItem.ingredientData.name) {
                    returnData.remove(item)
                    break
                }
            }
        }

        return returnData
    }
}