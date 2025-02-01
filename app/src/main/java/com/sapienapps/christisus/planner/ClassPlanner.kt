package com.sapienapps.christisus.planner

import android.content.Context

// Enums for Profile and Language
enum class Profile { B, N, M }
enum class Language { F, L }

interface ClassPlanner {

    val maxClasses: Int
    val maxStudentsPerClass: Int
    val allowedProfileCombinations: List<List<Profile>>
    val allowedLanguageCombinations: List<List<Language>>

    fun assignStudentsToClasses(students: List<Student>)

    fun optimizeClassAssignments()

    fun writeResultsToExcel(context: Context, outputPath: String):String
}
