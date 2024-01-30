package com.example.ezzpay

data class User(
    val name: String,
    val number: String,
    val email: String,
    val idCardNumber: String,
    val rating: Double,
    val reviews: Int,
    var balance: Double,
    var accountNumber: String,
    var points: Int
)
{
    // Secondary constructor for creating an empty User
    constructor() : this("", "", "", "", 0.0, 0, 0.0, "", 0)
}
