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
            add(IngredientData(uri, "삼겹살", "meat", "g",1))
            add(IngredientData(uri, "돼지 목살", "meat", "g",2))
            add(IngredientData(uri, "돼지 갈비", "meat", "g",3))
            add(IngredientData(uri, "닭 한마리", "meat", "g",4))
            add(IngredientData(uri, "닭가슴살", "meat", "g",5))
            add(IngredientData(uri, "소고기 등심", "meat", "g",6))
            add(IngredientData(uri, "소고기 안심", "meat", "g",7))
            add(IngredientData(uri, "소 갈비", "meat", "g",8))
            add(IngredientData(uri, "베이컨", "meat", "g",9))
            add(IngredientData(uri, "소시지", "meat", "g",10))
            add(IngredientData(uri, "계란", "meat", "g",11))
        }

        uri = getUriForResource(R.drawable.icon_seafood)
        ingredDatas.apply {
            add(IngredientData(uri, "연어", "seafood", "마리", 12))
            add(IngredientData(uri, "고등어", "seafood", "마리", 13))
            add(IngredientData(uri, "갈치", "seafood", "마리", 14))
            add(IngredientData(uri, "참치", "seafood", "마리", 15))
            add(IngredientData(uri, "멸치", "seafood", "마리", 16))
            add(IngredientData(uri, "새우", "seafood", "마리", 17))
            add(IngredientData(uri, "굴", "seafood", "개", 18))
            add(IngredientData(uri, "오징어", "seafood", "마리", 19))
            add(IngredientData(uri, "문어", "seafood", "마리", 20))
            add(IngredientData(uri, "미역", "seafood", "개", 21))
        }

        uri = getUriForResource(R.drawable.icon_diary_product)
        ingredDatas.apply {
            add(IngredientData(uri, "우유", "diary_product", "ml", 22))
            add(IngredientData(uri, "치즈", "diary_product", "개", 23))
            add(IngredientData(uri, "버터", "diary_product", "g", 24))
            add(IngredientData(uri, "생크림", "diary_product", "ml", 25))
        }

        uri = getUriForResource(R.drawable.icon_grain)
        ingredDatas.apply {
            add(IngredientData(uri, "쌀", "grain", "g", 26))
            add(IngredientData(uri, "보리", "grain", "g", 27))
            add(IngredientData(uri, "밀가루", "grain", "g", 28))
            add(IngredientData(uri, "옥수수", "grain", "g", 29))
            add(IngredientData(uri, "콩", "grain", "g", 30))
            add(IngredientData(uri, "면", "grain", "g", 31))
        }

        uri = getUriForResource(R.drawable.icon_vegetable)
        ingredDatas.apply {
            add(IngredientData(uri, "양파", "vegetable", "개", 32))
            add(IngredientData(uri, "마늘", "vegetable", "개", 33))
            add(IngredientData(uri, "당근", "vegetable", "개", 34))
            add(IngredientData(uri, "오이", "vegetable", "개", 35))
            add(IngredientData(uri, "배추", "vegetable", "개", 36))
            add(IngredientData(uri, "파", "vegetable", "개", 37))
            add(IngredientData(uri, "고추", "vegetable", "개", 38))
            add(IngredientData(uri, "깻잎", "vegetable", "개", 39))
            add(IngredientData(uri, "상추", "vegetable", "개", 40))
            add(IngredientData(uri, "콩나물", "vegetable", "개", 41))
            add(IngredientData(uri, "버섯", "vegetable", "개", 42))
            add(IngredientData(uri, "무", "vegetable", "개", 43))
            add(IngredientData(uri, "브로콜리", "vegetable", "개", 44))
            add(IngredientData(uri, "피망", "vegetable", "개", 45))
            add(IngredientData(uri, "감자", "vegetable", "개", 46))
        }

        uri = getUriForResource(R.drawable.icon_fruit)
        ingredDatas.apply {
            add(IngredientData(uri, "사과", "fruit", "개", 47))
            add(IngredientData(uri, "배", "fruit", "개", 48))
            add(IngredientData(uri, "바나나", "fruit", "개", 49))
            add(IngredientData(uri, "토마토", "fruit", "개", 50))
        }

        uri = getUriForResource(R.drawable.icon_sauce)
        ingredDatas.apply {
            add(IngredientData(uri, "간장", "sauce", "g", 51))
            add(IngredientData(uri, "고추장", "sauce", "g", 52))
            add(IngredientData(uri, "된장", "sauce", "g", 53))
            add(IngredientData(uri, "소금", "sauce", "g", 54))
            add(IngredientData(uri, "설탕", "sauce", "g", 55))
            add(IngredientData(uri, "토마토 소스", "sauce", "g", 56))
            add(IngredientData(uri, "식초", "sauce", "g", 57))
            add(IngredientData(uri, "참기름", "sauce", "g", 58))
            add(IngredientData(uri, "들기름", "sauce", "g", 59))
            add(IngredientData(uri, "초고추장", "sauce", "g", 60))
            add(IngredientData(uri, "물엿", "sauce", "g", 61))
            add(IngredientData(uri, "고춧가루", "sauce", "g", 62))
            add(IngredientData(uri, "돈까스 소스", "sauce", "g", 63))
            add(IngredientData(uri, "케찹", "sauce", "g", 64))
            add(IngredientData(uri, "머스타드", "sauce", "g", 65))
            add(IngredientData(uri, "후추", "sauce", "g", 66))
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