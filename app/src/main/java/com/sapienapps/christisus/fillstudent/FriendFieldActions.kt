package com.sapienapps.christisus.fillstudent

sealed interface InputField {
    data class Friend1(val position: Int) : InputField
    data class Friend2(val position: Int) : InputField
    data class UnFriend1(val position: Int) : InputField
    data class UnFriend2(val position: Int) : InputField
}
