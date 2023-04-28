package com.swef.cookcode.data

data class StepData(
    val imageData: List<String>,
    val videoData: List<String>?,
    val title: String,
    val description: String,
    val numberOfStep: Int
)
