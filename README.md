# cookcode
- 서비스 소개 : 냉장고 재료 관리 & 레시피 공유 커뮤니티   
   
## 2023-1학기 SW 캡스톤디자인

### Member 👨🏼‍🤝‍👨🏼

|                          신기철                           |                          김승은                           |                          박해인                          |                          노우영                          |
| :-------------------------------------------------------: | :-------------------------------------------------------: | :-------------------------------------------------------: | :-------------------------------------------------------: |
|                      소프트웨어학과                       |                      소프트웨어학과                       |                      소프트웨어학과                       |                      소프트웨어학과                       |
|                    팀장, 백엔드 개발자                    |                          백엔드 개발자                           |                        안드로이드 개발자                          |                            IOS 개발자                             |                        백엔드                           |
|    [@신기철](https://github.com/skck0226)     |        [@김승은](https://github.com/julie0005)         |           [@박해인](https://github.com/haeiny-cloud)            |         [@노우영](https://github.com/99Page)          |


* * *

해당 페이지는 Android 모바일 어플리케이션 프로젝트 관리 repository입니다.   

* * *

## Android 기술 스택

- 언어: `Kotlin`
- IDE : `Android Studio`
- 개발 버전 : `Android 11`
- 개발 환경 : `Pixel 3a(AVD), Galaxy S10e(SM-G970N)`
- 주요 프레임워크 : `Kotlin, Android, Retrofit2, OkHttp, ffmpeg`

## 주요 폴더 바로가기

- Source Codes : [Link](https://github.com/ajou-swef/cookcode-android/tree/main/cookcode/app/src/main/java/com/swef/cookcode)
- XML Layout Files : [Link](https://github.com/ajou-swef/cookcode-android/tree/main/cookcode/app/src/main/res/layout)
- Drawable Components : [Link](https://github.com/ajou-swef/cookcode-android/tree/main/cookcode/app/src/main/res/drawable)
- Total Resource Folder : [Link](https://github.com/ajou-swef/cookcode-android/tree/main/cookcode/app/src/main/res)

* * *

## 주요 개발 내용
1. Retrofit2를 사용한 REST API 구현
  - Retrofit은 OkHttp를 기반으로 작성된 라이브러리 입니다. 서버 통신을 위한 HTTP API를 Kotlin의 Interface로 간단하게 변환하여 API 호출을 쉽게 도와줍니다. 해당 프로젝트에서는 각 기능 별(쿠키, 레시피, 사용자 등)로 API Interface를 나누어 작성하였고, 사용하려는 REST API를 각 기능 별 API 객체변수를 만들어 간단하게 사용하고자 했습니다.    
  - 코드 예시 :   
  
[CookieAPI.kt](https://github.com/ajou-swef/cookcode-android/blob/main/cookcode/app/src/main/java/com/swef/cookcode/api/CookieAPI.kt)
<pre>
<code>
@DELETE("cookie/comments/{commentId}")
    fun deleteCookieComment(
        @Header("accessToken") accessToken: String,
        @Path("commentId") commentId: Int,
    ): Call<StatusResponse>
</code>
</pre>
[CommentRecyclerviewAdapter.kt](https://github.com/ajou-swef/cookcode-android/blob/main/cookcode/app/src/main/java/com/swef/cookcode/adapter/CommentRecyclerviewAdapter.kt)
<pre>
<code>
private fun deleteCookieComment(commentId: Int){
            cookieAPI.deleteCookieComment(accessToken, commentId).enqueue(object :
                Callback<StatusResponse> {
                override fun onResponse(
                    call: Call<StatusResponse>,
                    response: Response<StatusResponse>
                ) {
                    if (response.isSuccessful){
                        putToastMessage("댓글이 삭제되었습니다.")
                        listener.itemOnClick(cookieId)
                    }
                    else {
                        putToastMessage("에러 발생!")
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    putToastMessage("잠시 후 다시 시도해주세요.")
                }
            })
        }
</code>
</pre>
2. Coroutine을 활용하여 suspend function을 구현하고, 네트워크 통신 비동기 처리
  - 
3. ffmpeg를 사용하여 영상 병합 및 인코딩 처리
  -

### 실제 어플리케이션 시연 영상
