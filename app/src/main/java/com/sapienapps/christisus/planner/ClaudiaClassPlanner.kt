package com.sapienapps.christisus.planner

import android.content.Context
import com.sapienapps.christisus.excelreader.FileUtilsV2
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream

class ClaudiaClassPlanner(
    override val maxClasses: Int,
    override val maxStudentsPerClass: Int,
    override val allowedProfileCombinations: List<List<Profile>>,
    override val allowedLanguageCombinations: List<List<Language>>
) : ClassPlanner {
    private val classes = mutableListOf<ClassRoom>()
    private val conflictedStudents = mutableListOf<Student>()


    private fun canAssignToClass(student: Student, classroom: ClassRoom): Boolean {
        // Check if class is full
        if (classroom.students.size >= classroom.maxStudents) {
            return false
        }

        // Check if profile and language are allowed
        if (!classroom.allowedProfiles.contains(student.profile) ||
            !classroom.allowedLanguages.contains(student.language)
        ) {
            return false
        }

        // Check if any non-friends are in the class
        if (classroom.students.any { student.nonFriendsList.contains("${it.firstName} ${it.lastName}") }) {
            return false
        }

        return true
    }

    override fun optimizeClassAssignments() {
        var improvements = true
        var iterations = 0
        val maxIterations = 3 // Limit iterations to prevent infinite loops

        while (improvements && iterations < maxIterations) {
            improvements = false
            iterations++

            // For each class
            for (sourceClass in classes) {
                // For each student in the class
                val studentsToConsider = sourceClass.students.toList() // Create a copy to avoid concurrent modification
                for (student in studentsToConsider) {
                    // Calculate current friend count in this class
                    val currentFriendCount = sourceClass.students.count {
                        student.friendsList.contains("${it.firstName} ${it.lastName}")
                    }

                    // Look for a better class for this student
                    for (targetClass in classes.filter { it != sourceClass }) {
                        val potentialFriendCount = targetClass.students.count {
                            student.friendsList.contains("${it.firstName} ${it.lastName}")
                        }

                        if (shouldSwapStudent(
                                student,
                                sourceClass,
                                targetClass,
                                currentFriendCount,
                                potentialFriendCount
                            )
                        ) {
                            // Perform the swap
                            sourceClass.students.remove(student)
                            targetClass.students.add(student)
                            improvements = true
                            break
                        }
                    }
                }
            }

            // Try to assign conflicted students after each optimization round
            tryAssignConflictedStudents()
        }
    }

    private fun shouldSwapStudent(
        student: Student,
        sourceClass: ClassRoom,
        targetClass: ClassRoom,
        currentFriendCount: Int,
        potentialFriendCount: Int
    ): Boolean {
        // Check if target class has capacity
        if (targetClass.students.size >= targetClass.maxStudents) {
            return false
        }

        // Check if profile and language are allowed in target class
        if (!targetClass.allowedProfiles.contains(student.profile) ||
            !targetClass.allowedLanguages.contains(student.language)
        ) {
            return false
        }

        // Check for non-friends in target class
        if (targetClass.students.any { student.nonFriendsList.contains("${it.firstName} ${it.lastName}") }) {
            return false
        }

        // Calculate the impact on friend connections in both classes
        val sourceClassRemainingFriends = currentFriendCount - 1 // Subtract the student being moved
        val impactOnSourceClass = if (sourceClassRemainingFriends > 0) -1 else 0

        // Return true if the move would result in more friend connections overall
        return potentialFriendCount > (currentFriendCount + impactOnSourceClass)
    }

    private fun tryAssignConflictedStudents() {
        val remainingConflicts = conflictedStudents.toList()
        conflictedStudents.clear()

        for (student in remainingConflicts) {
            var assigned = false
            for (classroom in classes) {
                if (canAssignToClass(student, classroom)) {
                    classroom.students.add(student)
                    assigned = true
                    break
                }
            }
            if (!assigned) {
                conflictedStudents.add(student)
            }
        }
    }

    override fun assignStudentsToClasses(students: List<Student>) {
        // Initialize classes based on allowed combinations
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

        // First pass: Try to assign students with their friends
        for (student in students) {
            var assigned = false
            for (classroom in classes) {
                if (canAssignToClass(student, classroom)) {
                    classroom.students.add(student)
                    assigned = true
                    break
                }
            }
            if (!assigned) {
                conflictedStudents.add(student)
            }
        }

        // Run optimization after initial assignment
        optimizeClassAssignments()
    }

    override fun writeResultsToExcel(
        context: Context,
        outputPath: String,
        fileName: String
    ): String {
        return FileUtilsV2.writeResultsToExcel(
            context,
            outputPath,
            classes = classes,
            conflictedStudents = conflictedStudents,
            fileName = fileName
        )
    }

    fun getClasses(): List<ClassRoom> {
        return classes
    }

    fun getConflictedStudents(): List<Student> {
        return conflictedStudents
    }
}
