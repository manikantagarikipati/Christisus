package com.sapienapps.christisus.planner

import android.content.Context

class HumanClassPlanner(
    override val maxClasses: Int,
    override val maxStudentsPerClass: Int,
    override val allowedProfileCombinations: List<List<Profile>>,
    override val allowedLanguageCombinations: List<List<Language>>
):ClassPlanner {

    override fun assignStudentsToClasses(students: List<Student>) {
        //create groups of students
        val (studentGroups, conflictedStudents) = createStudentGroups(students)

        // Print or process results
        studentGroups.forEachIndexed { index, group ->
            println("Class ${index + 1}: ${group.map { it.firstName + " " + it.lastName }}")
        }
        println("Conflicted Students: ${conflictedStudents.map { it.firstName + " " + it.lastName }}")

        //assign groups to class
    }

    fun createStudentGroups(students: List<Student>): Pair<MutableList<List<Student>>, MutableList<Student>> {
        val studentsKeyValueMap = students.associateBy { it.lastName }
        val visited = mutableSetOf<String>()
        val studentGroups = mutableListOf<List<Student>>()
        val conflictedStudents = mutableListOf<Student>()


        // DFS function to group valid students
        fun dfs(
            lastName: String,
            group: MutableList<Student>,
            currentUnfriends: MutableSet<String>
        ): Boolean {
            if (lastName in visited) return true
            visited.add(lastName)

            val student = studentsKeyValueMap[lastName] ?: return true

            // 🛠 FIX: Check for conflicts before adding the student
            if (currentUnfriends.contains(lastName)) {
                conflictedStudents.add(student)
                return false // Conflict found, stop adding this student
            }

            group.add(student)

            // Extend the unfriend list for future students in this group
            currentUnfriends.addAll(student.nonFriendsList)

            // Traverse friends
            for (friend in student.friendsList) {
                if (studentsKeyValueMap.containsKey(friend)) {
                    if (!dfs(friend, group, currentUnfriends)) {
                        // 🛠 FIX: Ensure we remove students that cause conflicts
                        group.remove(student)
                        conflictedStudents.add(student)
                        return false
                    }
                }
            }
            return true
        }


        // Iterate through each student and form groups
        for (student in students) {
            if (student.lastName !in visited) {
                val group = mutableListOf<Student>()
                val currentUnfriends = mutableSetOf<String>()
                if (dfs(student.lastName, group, currentUnfriends)) {
                    studentGroups.add(group)
                }
            }
        }
        return Pair(studentGroups, conflictedStudents)
    }

    override fun optimizeClassAssignments() {

    }

    override fun writeResultsToExcel(context: Context, outputPath: String): String {
        return ""
    }
}
