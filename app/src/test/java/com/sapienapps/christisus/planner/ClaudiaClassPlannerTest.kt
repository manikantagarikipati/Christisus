package com.sapienapps.christisus.planner

import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ClaudiaClassPlannerTest {

    private lateinit var classPlanner: ClaudiaClassPlanner

    @Before
    fun setUp() {
        classPlanner = ClaudiaClassPlanner(
            maxClasses = 3,
            maxStudentsPerClass = 2,
            allowedProfileCombinations = listOf(
                listOf(Profile.B, Profile.N),
                listOf(Profile.M)
            ),
            allowedLanguageCombinations = listOf(
                listOf(Language.F),
                listOf(Language.L)
            )
        )
    }

    @Test
    fun `should assign students to classes`() {
        val students = listOf(
            Student("Alice", "Smith", Profile.B, Language.F),
            Student("Bob", "Johnson", Profile.N, Language.F),
            Student("Charlie", "Brown", Profile.M, Language.L)
        )

        classPlanner.assignStudentsToClasses(students)

        val classes = classPlanner.getClasses()
        assertEquals(3, classes.size)
        assertEquals(2, classes[0].students.size)
        assertEquals(1, classes[1].students.size)
    }

    @Test
    fun `should optimize class assignments`() {
        val students = listOf(
            Student("Alice", "Smith", Profile.B, Language.F, mutableListOf("Johnson")),
            Student("Bob", "Johnson", Profile.N, Language.F, mutableListOf("Smith")),
            Student("Charlie", "Brown", Profile.M, Language.L)
        )

        classPlanner.assignStudentsToClasses(students)
        classPlanner.optimizeClassAssignments()

        val classes = classPlanner.getClasses()
        assertEquals(3, classes.size)
        assertTrue(classes[0].students.any { it.firstName == "Alice" && it.lastName == "Smith" })
        assertTrue(classes[0].students.any { it.firstName == "Bob" && it.lastName == "Johnson" })
    }

    @Ignore
    @Test
    fun `should handle conflicted students`() {
        val students = listOf(
            Student("Alice", "Smith", Profile.B, Language.F, mutableListOf(), mutableListOf("Johnson")),
            Student("Bob", "Johnson", Profile.N, Language.F, mutableListOf(), mutableListOf("Smith")),
            Student("Charlie", "Brown", Profile.M, Language.L)
        )

        classPlanner.assignStudentsToClasses(students)

        val conflictedStudents = classPlanner.getConflictedStudents()
        assertEquals(2, conflictedStudents.size)
        assertTrue(conflictedStudents.any { it.firstName == "Alice" && it.lastName == "Smith" })
        assertTrue(conflictedStudents.any { it.firstName == "Bob" && it.lastName == "Johnson" })
    }
}
