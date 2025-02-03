package com.sapienapps.christisus.planner


import org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class HumanClassPlannerTest {

    private lateinit var classPlanner: HumanClassPlanner

    @Before
    fun setUp() {
        classPlanner = HumanClassPlanner(
            maxClasses = 5,
            maxStudentsPerClass = 3,
            allowedProfileCombinations = listOf(),  // Not testing profiles yet
            allowedLanguageCombinations = listOf()  // Not testing languages yet
        )
    }

    @Test
    fun `should group friends together`() {
        val students = listOf(
            Student("Alice", "Smith", Profile.B, Language.F, mutableListOf("Johnson")),
            Student("Bob", "Johnson", Profile.B, Language.F, mutableListOf("Smith"))
        )

        val (studentGroups, conflictedStudents) = classPlanner.createStudentGroups(students)

        assertEquals(1, studentGroups.size)
        assertEquals(2, studentGroups[0].size)
        assertTrue(conflictedStudents.isEmpty())
    }

    @Test
    fun `should separate conflicted students`() {
        val students = listOf(
            Student("Alice", "Smith", Profile.B, Language.F, mutableListOf("Johnson"), mutableListOf("Brown")),
            Student("Bob", "Johnson", Profile.B, Language.F, mutableListOf("Brown")),
            Student("Charlie", "Brown", Profile.B, Language.F, mutableListOf("Williams"), mutableListOf("Smith"))
        )

        val (studentGroups, conflictedStudents) = classPlanner.createStudentGroups(students)

        assertEquals(0, studentGroups.size) // Smith & Johnson should be in the same group
        assertEquals(3, conflictedStudents.size)
    }

    @Test
    fun `should form separate groups for unrelated students`() {
        val students = listOf(
            Student("Alice", "Smith", Profile.B, Language.F),
            Student("Bob", "Johnson", Profile.B, Language.F)
        )

        val (studentGroups, conflictedStudents) = classPlanner.createStudentGroups(students)

        assertEquals(2, studentGroups.size) // Each student should form their own group
        assertTrue(conflictedStudents.isEmpty())
    }

    @Test
    fun `should handle an empty student list gracefully`() {
        val students = emptyList<Student>()

        val (studentGroups, conflictedStudents) = classPlanner.createStudentGroups(students)

        assertTrue(studentGroups.isEmpty())
        assertTrue(conflictedStudents.isEmpty())
    }

    @Test
    fun `should not place an unfriended student in the same group`() {
        val students = listOf(
            Student("Alice", "Smith", Profile.B, Language.F, mutableListOf(), mutableListOf("Johnson")),
            Student("Bob", "Johnson", Profile.B, Language.F, mutableListOf(), mutableListOf("Smith"))
        )

        val (studentGroups, conflictedStudents) = classPlanner.createStudentGroups(students)

        assertEquals(2, studentGroups.size) // Smith and Johnson should be in separate groups
        assertEquals(0, conflictedStudents.size)
    }
}


