package com.example.projekt.Budgets
data class Budgets(
    var name: String = "",
    var category: String = "",
    var monthlyLimit: Double = 0.0,
    var yearlyLimit: Double = 0.0,
    var userId: String = ""
)