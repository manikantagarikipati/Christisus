package com.sapienapps.christisus.selectclasscombo

data class ClassInfo(
    val name: String,
    var profile1: String ="",
    var profile2: String ="",
    var profile3: String="",
    var language: String="",
    var profile1Position: Int = 0,
    var profile2Position: Int = 0,
    var profile3Position: Int = 0,
    var languagePosition: Int = 0
)
