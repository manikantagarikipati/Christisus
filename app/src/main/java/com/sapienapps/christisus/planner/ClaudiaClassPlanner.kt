package com.sapienapps.christisus.planner

import android.content.Context
import com.sapienapps.christisus.excelreader.FileUtilsV2
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.random.Random


class ClaudiaClassPlanner(
    override val maxClasses: Int,
    override val maxStudentsPerClass: Int,
    override val allowedProfileCombinations: List<List<Profile>>,
    override val allowedLanguageCombinations: List<List<Language>>
) : ClassPlanner {
    private val classes = mutableListOf<ClassRoom>()
    private val conflictedStudents = mutableListOf<Student>()

    fun readStudentsFromExcel(filePath: String): List<Student> {
        val students = mutableListOf<Student>()
        val workbook = WorkbookFactory.create(FileInputStream(File(filePath)))
        val sheet = workbook.getSheetAt(0)

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            val student = Student(
                firstName = row.getCell(0).stringCellValue,
                lastName = row.getCell(1).stringCellValue,
                profile = Profile.valueOf(row.getCell(2).stringCellValue),
                language = Language.valueOf(row.getCell(3).stringCellValue)
            )

            // Parse friends list
            val friendsCell = row.getCell(4).stringCellValue
            if (friendsCell.isNotEmpty()) {
                student.friendsList.addAll(friendsCell.split(",").map { it.trim() })
            }

            // Parse non-friends list
            val nonFriendsCell = row.getCell(5).stringCellValue
            if (nonFriendsCell.isNotEmpty()) {
                student.nonFriendsList.addAll(nonFriendsCell.split(",").map { it.trim() })
            }

            students.add(student)
        }
        workbook.close()
        return students
    }

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
        val nonFriendsInClass = classroom.students.any {
            student.nonFriendsList.contains("${it.firstName} ${it.lastName}")
        }
        if (nonFriendsInClass) {
            return false
        }

        // If student has friends listed, prefer classes where friends are present
        if (student.friendsList.isNotEmpty()) {
            val friendsInClass = classroom.students.count {
                student.friendsList.contains("${it.firstName} ${it.lastName}")
            }

            // If this is not the first class we're checking (indicated by other students being present)
            // and none of the student's friends are in this class, try to find a better class
            if (classroom.students.isNotEmpty() && friendsInClass == 0) {
                return false
            }
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
                val studentsToConsider =
                    sourceClass.students.toList() // Create a copy to avoid concurrent modification
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
        val hasNonFriendsInTarget = targetClass.students.any {
            student.nonFriendsList.contains("${it.firstName} ${it.lastName}")
        }
        if (hasNonFriendsInTarget) {
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
            val languageCombination =
                allowedLanguageCombinations[i % allowedLanguageCombinations.size]
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

    override fun writeResultsToExcel(context: Context, outputPath: String): String {
        return FileUtilsV2.writeResultsToExcel(
            context,
            outputPath,
            classes = classes,
            conflictedStudents = conflictedStudents
        )
    }
}

