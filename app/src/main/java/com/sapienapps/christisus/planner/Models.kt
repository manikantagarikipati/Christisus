package com.sapienapps.christisus.planner

// Data classes to represent our entities
data class Student(
    val firstName: String,
    val lastName: String,
    val profile: Profile,
    val language: Language,
    val friendsList: MutableList<String> = mutableListOf(),
    val nonFriendsList: MutableList<String> = mutableListOf()
) {
    fun fullName(): String {
        return "$firstName $lastName"
    }

    fun reverseFullName(): String {
        return "$lastName $firstName"
    }
}

data class ClassRoom(
    val id: Int,
    val students: MutableList<Student> = mutableListOf(),
    val allowedProfiles: List<Profile>,
    val allowedLanguages: List<Language>,
    val maxStudents: Int
)
