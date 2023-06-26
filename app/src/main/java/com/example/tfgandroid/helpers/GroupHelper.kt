package com.example.tfgandroid.helpers

class GroupHelper {
    companion object {
        fun generateRandomPrefix(length: Int): String {
            val allowedCharacters = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return List(length) { allowedCharacters.random() }.joinToString("")
        }
    }
}