package com.sapienapps.christisus.claudiacode

import android.content.Context
import android.widget.ListView.FixedViewInfo
import com.sapienapps.christisus.fillstudent.StudentInfoViewData
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.random.Random

// Data classes to represent our entities
data class Student(
    val firstName: String,
    val lastName: String,
    val profile: Profile,
    val language: Language,
    val friendsList: MutableList<String> = mutableListOf(),
    val nonFriendsList: MutableList<String> = mutableListOf()
)

data class ClassRoom(
    val id: Int,
    val students: MutableList<Student> = mutableListOf(),
    val allowedProfiles: List<Profile>,
    val allowedLanguages: List<Language>,
    val maxStudents: Int
)

// Enums for Profile and Language
enum class Profile { B, N, M }
enum class Language { F, L }

class StudentClassPlanner(
    private val maxClasses: Int,
    private val maxStudentsPerClass: Int,
    private val allowedProfileCombinations: List<List<Profile>>,
    private val allowedLanguageCombinations: List<List<Language>>
) {
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
            !classroom.allowedLanguages.contains(student.language)) {
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

    fun optimizeClassAssignments() {
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
                            )) {
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
            !targetClass.allowedLanguages.contains(student.language)) {
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

    fun assignStudentsToClasses(students: List<Student>) {
        // Initialize classes based on allowed combinations
        for (i in 0 until maxClasses) {
            val profileCombination = allowedProfileCombinations[i % allowedProfileCombinations.size]
            val languageCombination = allowedLanguageCombinations[i % allowedLanguageCombinations.size]
            classes.add(ClassRoom(
                id = i + 1,
                allowedProfiles = profileCombination,
                allowedLanguages = languageCombination,
                maxStudents = maxStudentsPerClass
            ))
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

    fun writeResultsToExcel(context: Context,outputPath: String):String {
        val workbook = XSSFWorkbook()

        // Create sheet for assigned students
        val assignedSheet = workbook.createSheet("Assigned Classes")
        var rowNum = 0

        // Write header
        val headerRow = assignedSheet.createRow(rowNum++)
        headerRow.createCell(0).setCellValue("Class")
        headerRow.createCell(1).setCellValue("First Name")
        headerRow.createCell(2).setCellValue("Last Name")
        headerRow.createCell(3).setCellValue("Profile")
        headerRow.createCell(4).setCellValue("Language")
        headerRow.createCell(5).setCellValue("Friends In Class")

        // Write assigned students
        for (classroom in classes) {
            for (student in classroom.students) {
                val row = assignedSheet.createRow(rowNum++)
                row.createCell(0).setCellValue("${classroom.id}")
                row.createCell(1).setCellValue(student.firstName)
                row.createCell(2).setCellValue(student.lastName)
                row.createCell(3).setCellValue(student.profile.toString())
                row.createCell(4).setCellValue(student.language.toString())

                // Get friends who are in the same class
                val friendsInClass = classroom.students
                    .filter { "${it.firstName} ${it.lastName}" in student.friendsList }
                    .map { "${it.firstName} ${it.lastName}" }
                    .joinToString(", ")
                row.createCell(5).setCellValue(friendsInClass)
            }
        }

        // Create sheet for conflicted students
        val conflictSheet = workbook.createSheet("Conflicts")
        rowNum = 0

        // Write header for conflicts
        val conflictHeaderRow = conflictSheet.createRow(rowNum++)
        conflictHeaderRow.createCell(0).setCellValue("First Name")
        conflictHeaderRow.createCell(1).setCellValue("Last Name")
        conflictHeaderRow.createCell(2).setCellValue("Profile")
        conflictHeaderRow.createCell(3).setCellValue("Language")
        conflictHeaderRow.createCell(4).setCellValue("Reason")

        // Write conflicted students
        for (student in conflictedStudents) {
            val row = conflictSheet.createRow(rowNum++)
            row.createCell(0).setCellValue(student.firstName)
            row.createCell(1).setCellValue(student.lastName)
            row.createCell(2).setCellValue(student.profile.toString())
            row.createCell(3).setCellValue(student.language.toString())
            row.createCell(4).setCellValue("Could not assign to any class")
        }

        val fileNumber = Random.nextInt(2000)
        val fileName = "ChristiusFinalClassList${fileNumber}.xlsx"
        val fileDir = context.getExternalFilesDir(null) // Use the app's external files directory
        val file = File(fileDir, fileName)

        // Write the workbook to file
        FileOutputStream(file).use {
            workbook.write(it)
        }
        workbook.close()
        return file.absolutePath // Return the file path
    }
}

fun createMasterFile(context:Context,studentViewInfo: List<StudentInfoViewData>):String{
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("MasterStudentList")
    val headerRow = sheet.createRow(0)

    headerRow.createCell(0).setCellValue("Name")
    headerRow.createCell(1).setCellValue("First Name")
    headerRow.createCell(2).setCellValue("Profile")
    headerRow.createCell(3).setCellValue("Language")
    headerRow.createCell(4).setCellValue("Friend 1")
    headerRow.createCell(5).setCellValue("Friend 2")
    headerRow.createCell(4).setCellValue("Non Friend1")
    headerRow.createCell(5).setCellValue("Non Friend2")

    for(i in studentViewInfo.indices){
        val row = sheet.createRow(i+1)
        row.createCell(0).setCellValue(studentViewInfo[i].name)
        row.createCell(1).setCellValue(studentViewInfo[i].firstName)
        row.createCell(2).setCellValue(studentViewInfo[i].Profile)
        row.createCell(3).setCellValue(studentViewInfo[i].language)
        row.createCell(4).setCellValue(studentViewInfo[i].friend1)
        row.createCell(5).setCellValue(studentViewInfo[i].friend2)
        row.createCell(6).setCellValue(studentViewInfo[i].unFriend1)
        row.createCell(7).setCellValue(studentViewInfo[i].unFriend2)
    }

    val fileNumber = Random.nextInt(2000)
    val fileName = "ChristiusMasterStudentFileWithFriendInfo${fileNumber}.xlsx"

    val fileDir = context.getExternalFilesDir(null) // Use the app's external files directory
    val file = File(fileDir, fileName)

    FileOutputStream(file).use {
        workbook.write(it)
    }
    workbook.close()
    return file.absolutePath // Return the file path
}

