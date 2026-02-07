package com.example.visionstock.item

data class UserItem(
    // DEFAULT VALUES (= "") ARE REQUIRED FOR FIREBASE TO WORK
    var userId: String = "",
    var username: String = "",
    var email: String = "",
    var role: String = "",
    var status: String = "",
    var password: String = "" // Keep this if you use it locally, even if not in DB
)