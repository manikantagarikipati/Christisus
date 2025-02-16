package com.sapienapps.christisus.planner

import android.content.Context
import com.sapienapps.christisus.excelreader.FileUtilsV2
import com.sapienapps.christisus.selectclasscombo.ClassInfo

class ManiClassPlanner(
    override val maxClasses: Int,
    override val maxStudentsPerClass: Int,
    override val allowedProfileCombinations: List<List<Profile>>,
    override val allowedLanguageCombinations: List<List<Language>>
):ClassPlanner {
    private val unassignedStudents = mutableListOf<Student>()
    val classes = List(maxClasses) { ClassInfo("Class_${it + 1}") }

    override fun assignStudentsToClasses(students: List<Student>) {
        val assignedStudents = mutableSetOf<Student>()
        val studentQueue = students.toMutableList()

        studentQueue.sortWith(compareBy({ it.profile }, { it.language }))

        while (studentQueue.isNotEmpty()) {
            val student = studentQueue.removeFirst()
            val possibleClasses = classes.filter { classInfo ->
                classInfo.students.none { it.nonFriendsList.contains(student.firstName) } &&
                        allowedProfileCombinations.any { student.profile in it } &&
                        allowedLanguageCombinations.any { student.language in it }
            }

            val assignedClass = possibleClasses.minByOrNull { it.students.size }
            if (assignedClass != null) {
                if (assignedClass.students.size < maxStudentsPerClass) {
                    assignedClass.students.add(student)
                    assignedStudents.add(student)
                } else {
                    // Check for a student to swap out
                    val removableStudent = assignedClass.students.find {
                        it.friendsList.isEmpty() || it.friendsList.none { friend -> assignedClass.students.any { s -> s.firstName == friend } }
                    }
                    if (removableStudent != null) {
                        assignedClass.students.remove(removableStudent)
                        assignedStudents.remove(removableStudent)
                        assignedClass.students.add(student)
                        assignedStudents.add(student)
                        studentQueue.add(removableStudent) // Reassign removed student
                    } else {
                        unassignedStudents.add(student)
                    }
                }
            } else {
                unassignedStudents.add(student)
            }
        }

        // Optimization: Move students to better classes
        for (classInfo in classes) {
            for (student in classInfo.students.toList()) {
                val betterClass = classes.find {
                    it.students.size < maxStudentsPerClass &&
                            student.friendsList.any { friend -> it.students.any { s -> s.firstName == friend } }
                }
                if (betterClass != null && betterClass != classInfo) {
                    classInfo.students.remove(student)
                    betterClass.students.add(student)
                }
            }
        }
    }

    override fun optimizeClassAssignments() {
        // No more optimisation required
        Unit
    }

    override fun writeResultsToExcel(context: Context, outputPath: String): String {
        return FileUtilsV2.writeResultsToExcel(
            context,
            outputPath,
            classes = classes,
            conflictedStudents = unassignedStudents
        )
    }
}
