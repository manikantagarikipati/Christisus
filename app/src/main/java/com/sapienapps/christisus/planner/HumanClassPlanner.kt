package com.sapienapps.christisus.planner

import android.content.Context
import com.sapienapps.christisus.excelreader.FileUtilsV2

class HumanClassPlanner(
    override val maxClasses: Int,
    override val maxStudentsPerClass: Int,
    override val allowedProfileCombinations: List<List<Profile>>,
    override val allowedLanguageCombinations: List<List<Language>>
):ClassPlanner {

    private val classes = mutableListOf<ClassRoom>()
    private val conflictedStudents = mutableListOf<Student>()

    override fun assignStudentsToClasses(students: List<Student>) {
        //create groups of students
        //assign groups to class
    }

    override fun optimizeClassAssignments() {

    }

    override fun writeResultsToExcel(context: Context, outputPath: String): String {
        return ""
    }
}
