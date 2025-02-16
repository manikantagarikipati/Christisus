package com.sapienapps.christisus.selectclasscombo

import com.sapienapps.christisus.planner.ClassRoom
import com.sapienapps.christisus.planner.Student

class ClassComboSuggester {
    fun suggestSimpleClasses(
        students: List<Student>
    ): Pair<List<ClassRoom>, Int> {
        // Group students by their profile and language combination
        val profileLanguageGroups = students.groupBy { Pair(it.profile, it.language) }

        val classes = mutableListOf<ClassRoom>()
        var classId = 1

        // Estimate a simple max students per class
        val totalStudents = students.size
        val estimatedMaxStudentsPerClass = Math.max(1, totalStudents / profileLanguageGroups.size)

        // Iterate over each profile-language group
        for ((profileLanguage, studentGroup) in profileLanguageGroups) {
            val unassignedStudents = studentGroup.toMutableList()

            while (unassignedStudents.isNotEmpty()) {
                val classGroup = unassignedStudents.take(estimatedMaxStudentsPerClass)
                unassignedStudents.removeAll(classGroup)

                // Create a new class with the assigned students
                val newClass = ClassRoom(
                    id = classId++,
                    allowedProfiles = listOf(profileLanguage.first),
                    allowedLanguages = listOf(profileLanguage.second),
                    maxStudents = estimatedMaxStudentsPerClass,
                    students = classGroup.toMutableList()
                )
                classes.add(newClass)
            }
        }

        // Determine the maximum class size after distribution
        val maxStudentsPerClass = classes.maxOfOrNull { it.students.size } ?: 0

        return Pair(classes, maxStudentsPerClass)
    }




    fun generateClassSuggestionString(
        students: List<Student>
    ): String {
        val (classes, maxStudentsPerClass) = suggestSimpleClasses(students)

        val classCount = classes.size
        val studentCount = maxStudentsPerClass

        val stringBuilder = StringBuilder()
        stringBuilder.append("Suggest $classCount classes with $studentCount students per class, below combinations:\n")

        for (classRoom in classes) {
            val profileCombinations = classRoom.allowedProfiles.joinToString(", ") { it.name }
            val languageCombinations = classRoom.allowedLanguages.joinToString(", ") { it.name }
            stringBuilder.append("Class ${classRoom.id} with profile [$profileCombinations] language [$languageCombinations]\n")
        }

        return stringBuilder.toString()
    }

}
