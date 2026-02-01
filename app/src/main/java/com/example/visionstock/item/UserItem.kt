package com.example.visionstock.item // <--- THIS MUST MATCH THE FOLDER 'item'

data class UserItem(
    val userId: String,
    val username: String,
    val password: String,
    val status: String
)