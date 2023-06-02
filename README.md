# cookcode
- 서비스 소개 : 냉장고 재료 관리 & 레시피 공유 커뮤니티

해당 프로젝트는 다음과 같은 Needs를 충족시키기 위하여 제작되었습니다.
* 내가 가진 식재료를 이용해서 어떤 것을 만들 수 있을까? 
* 내가 요리한 과정을 다른 사람과 공유하고 싶다. 

레시피를 쉽게 만들기위해서 미리보기 기능을 제공하고 미리보기 중 수정이 가능합니다. 

유튜브 쇼츠처럼 짧은 영상을 만들어 제공하는 `쿠키` 기능을 제공합니다. 

[이곳](https://www.youtube.com/playlist?list=PLow6eB4W8f0fgjKj2_k8eavCbxeajqtBL)에서 모든 프로젝트 영상을 확인할 수 있습니다. 아래에는 일부 영상을 게시했습니다. 
   
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
  - Coroutine은 비동기 작업을 수행함으로써 Main Thread(UI Thread)에서 화면을 로딩하기 위해 많은 작업을 하는 것을 피하고, 백그라운드에서 작업함으로써 성능 부하를 줄여주는 기능입니다. Coroutine을 사용하기 위해서는 suspend function이라는 비동기 작업 함수를 만들어야 합니다. 해당 프로젝트에서는 대용량 파일인 비디오를 다운로드 받는 쿠키 조회 작업에서 비동기 처리를 위해 Coroutine과 suspend function을 사용하였습니다.
  - 코드 예시 : 

[CookieViewpagerAdapter.kt](https://github.com/ajou-swef/cookcode-android/blob/main/cookcode/app/src/main/java/com/swef/cookcode/adapter/CookieViewpagerAdapter.kt)
<pre>
<code>
fun bind(item: CookieData) {
         binding.cookie.setBackgroundResource(R.drawable.loading_video_page)
         binding.progressBar.visibility = View.VISIBLE

         initModifyDeleteButton(item.cookieId)

         CoroutineScope(Dispatchers.Main).launch {
             val videoUri = withContext(Dispatchers.IO) {
                 prepareVideo(item.videoUrl)
             }

             if (videoUri != null) {
                 binding.cookie.setBackgroundResource(0)
                 binding.cookie.setVideoURI(videoUri)
                 binding.progressBar.visibility = View.GONE
                 ...
</code>
</pre>
<pre>
<code>
private suspend fun prepareVideo(videoUrl: String): Uri? = withContext(Dispatchers.IO) {
      return@withContext downloadVideo(videoUrl)
}
        
private suspend fun downloadVideo(videoUrl: String): Uri? = withContext(Dispatchers.IO) {
      val client = OkHttpClient.Builder().build()
      val request = Request.Builder()
          .url(videoUrl)
          .build()

      val response = client.newCall(request).execute()
      val responseBody: ResponseBody? = response.body

      if (response.isSuccessful && responseBody != null) {
          val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
          val cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
          val tempFile = File.createTempFile("VIDEO_$timeStamp", ".mp4", cacheDir)

          val inputStream = responseBody.byteStream()
          val outputStream = FileOutputStream(tempFile)

          inputStream.use { input ->
              outputStream.use { output ->
                  input.copyTo(output)
              }
          }

          return@withContext Uri.parse(tempFile.absolutePath)
      }

      return@withContext null
}
</code>
</pre>
3. ffmpeg를 사용하여 영상 병합 및 인코딩 처리
  - 쿠키를 제작할 때 짧은 영상들을 업로드하고 하나의 영상으로 병합처리하여 서버에 업로드 해야하기 때문에 ffmpeg라는 영상 인코딩 라이브러리를 사용합니다. ffmpeg의 사용 방법은 명령 프롬프트 창에서 사용하듯이 명령어를 만들어 execute 해야합니다. 쿠키는 모바일 화면에 최적화 되어야 하기 때문에 세로로 긴 720x1080 크기로 인코딩하여 병합해주었습니다. 또한, 병합된 영상의 용량이 너무 커지는 것을 방지하기 위해 낮은 비트레이트로 인코딩 하되 영상의 품질은 최대한 살릴 수 있도록 옵션을 조절했습니다.
  - 코드 예시 : 
  
[CookieFormActivity.kt](https://github.com/ajou-swef/cookcode-android/blob/main/cookcode/app/src/main/java/com/swef/cookcode/CookieFormActivity.kt)
<pre>
<code>
fun mergeVideos(videoPaths: List<String>): Int {
        mergedVideoFile = this.filesDir.absolutePath + "/merged_video.mp4"

        val command = StringBuilder()

        command.append("-y")
        for (videoPath in videoPaths) {
            command.append(" -i ").append(videoPath)
        }
        command.append(" -filter_complex ").append("\"")
        for (i in videoPaths.indices) {
            command.append("[$i:v]setpts=PTS-STARTPTS,scale=720x1080,fps=24,format=yuv420p[v$i];")
            command.append("[$i:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo[a$i];")
        }
        for (i in videoPaths.indices) {
            command.append("[v$i][a$i]")
        }
        command.append("concat=n=").append(videoPaths.size).append(":v=1:a=1[v][a]").append("\"")
        command.append(" -map \"[v]\" -map \"[a]\" ").append("-vsync 0 ").append(mergedVideoFile)

        return FFmpeg.execute(command.toString())
    }
</code>
</pre>
### 실제 어플리케이션 시연 영상
