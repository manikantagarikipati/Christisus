package com.sapienapps.christisus.planner

import android.content.Context
import com.sapienapps.christisus.excelreader.FileUtilsV2
import com.sapienapps.christisus.selectclasscombo.ClassInfo
import kotlin.random.Random

class ManiClassPlanner(
    override val maxClasses: Int,
    override val maxStudentsPerClass: Int,
    override val allowedProfileCombinations: List<List<Profile>>,
    override val allowedLanguageCombinations: List<List<Language>>
) : ClassPlanner {
    private val unassignedStudents = mutableListOf<Student>()
    val classes = mutableListOf<ClassRoom>()

    override fun assignStudentsToClasses(students: List<Student>) {

        for (i in 0 until maxClasses) {
            val profileCombination = allowedProfileCombinations[i % allowedProfileCombinations.size]
            val languageCombination = allowedLanguageCombinations[i % allowedLanguageCombinations.size]
            classes.add(
                ClassRoom(
                    id = i + 1,
                    allowedProfiles = profileCombination,
                    allowedLanguages = languageCombination,
                    maxStudents = maxStudentsPerClass
                )
            )
        }

        val assignedStudents = mutableSetOf<Student>()
        val studentQueue = students.toMutableList()
        val unassignedStudents = mutableListOf<Student>()

        studentQueue.sortWith(compareBy({ it.profile }, { it.language }))

        while (studentQueue.isNotEmpty()) {
            val student = studentQueue.removeFirst()
            val possibleClasses = classes.filter { classRoom ->
                classRoom.students.none { it.nonFriendsList.contains(student.firstName) } &&
                        classRoom.allowedProfiles.contains(student.profile) &&
                        classRoom.allowedLanguages.contains(student.language)
            }

            val assignedClass = possibleClasses.minByOrNull { it.students.size }
            if (assignedClass != null) {
                if (assignedClass.students.size < maxStudentsPerClass) {
                    assignedClass.students.add(student)
                    assignedStudents.add(student)
                } else {
                    val removableStudent = assignedClass.students.find {
                        it.friendsList.isEmpty() || it.friendsList.none { friend -> assignedClass.students.any { s -> s.firstName == friend } }
                    }
                    if (removableStudent != null) {
                        assignedClass.students.remove(removableStudent)
                        assignedStudents.remove(removableStudent)
                        assignedClass.students.add(student)
                        assignedStudents.add(student)
                        studentQueue.add(removableStudent)
                    } else {
                        unassignedStudents.add(student)
                    }
                }
            } else {
                unassignedStudents.add(student)
            }
        }

        for (classRoom in classes) {
            for (student in classRoom.students.toList()) {
                val betterClass = classes.find {
                    it.students.size < maxStudentsPerClass &&
                            student.friendsList.any { friend -> it.students.any { s -> s.firstName == friend } }
                }
                if (betterClass != null && betterClass != classRoom) {
                    classRoom.students.remove(student)
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
