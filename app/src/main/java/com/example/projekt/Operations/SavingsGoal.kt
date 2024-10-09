package com.example.projekt.Operations

data class SavingsGoal(
    val goalId: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val endDate: String = "",
    val userId: String = ""
)
