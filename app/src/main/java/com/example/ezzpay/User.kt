package com.example.ezzpay

data class User(
    val name: String,
    val number: String,
    val email: String,
    val idCardNumber: String,
    val rating: Double,
    val reviews: Int,
    val balance: Double,
    val accountNumber: String,
    val points: Int
)
