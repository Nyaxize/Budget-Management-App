package com.example.projekt.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.Budgets.BudgetActivity
import com.example.projekt.Budgets.Budgets
import com.example.projekt.Notifications.NotificationHelper
import com.example.projekt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddBudgetActivity : AppCompatActivity() {

    private lateinit var categoryIcons: GridLayout
    private lateinit var database: DatabaseReference
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)
        supportActionBar?.title = "Add New Budget"

        categoryIcons = findViewById(R.id.categoryIcons)
        database = FirebaseDatabase.getInstance().reference.child("budgets")

        loadCategoryIcons()
    }

    private fun loadCategoryIcons() {
        val categories = listOf("Food", "Transport")
        for (category in categories) {
            val imageView = ImageView(this).apply {
                setImageResource(getCategoryIcon(category))
                contentDescription = category
                layoutParams = GridLayout.LayoutParams().apply {
                    width = resources.getDimensionPixelSize(R.dimen.icon_size)
                    height = resources.getDimensionPixelSize(R.dimen.icon_size)
                }
                setPadding(8, 8, 8, 8)
                setOnClickListener {
                    selectedCategory = category
                    highlightSelectedCategory(this, categoryIcons)
                }
            }
            categoryIcons.addView(imageView)
        }
    }

    private fun highlightSelectedCategory(selectedView: ImageView, categoryIcons: GridLayout) {
        for (i in 0 until categoryIcons.childCount) {
            val child = categoryIcons.getChildAt(i)
            child.alpha = if (child == selectedView) 1.0f else 0.5f
        }
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category) {
            "Food" -> R.drawable.ic_burger
            "Transport" -> R.drawable.ic_bus
            else -> R.drawable.ic_notification
        }
    }

    fun saveBudget(view: View) {
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val monthlyLimitEditText = findViewById<EditText>(R.id.monthlyLimitEditText)
        val yearlyLimitEditText = findViewById<EditText>(R.id.yearlyLimitEditText)

        val name = nameEditText.text.toString()
        val monthlyLimit = monthlyLimitEditText.text.toString().toDoubleOrNull() ?: 0.0
        val yearlyLimit = yearlyLimitEditText.text.toString().toDoubleOrNull() ?: 0.0
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val budgetId = database.child(userId).push().key
        if (budgetId != null) {
            val budget = Budgets(name, selectedCategory ?: "Uncategorized", monthlyLimit, yearlyLimit, userId)
            database.child(userId).child(budgetId).setValue(budget).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, BudgetActivity::class.java)
                    startActivity(intent)
                    val notificationHelper = NotificationHelper(this)
                    notificationHelper.sendNotification(
                        "Budget Added",
                        "Budget for category $selectedCategory has been added successfully."
                    )
                    finish() // Powrót do poprzedniej aktywności
                } else {
                    // Obsługa błędu
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.Back_button -> {
                Toast.makeText(this, "You Backed to Main Menu", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            R.id.Settings -> {
                // Zrealizuj akcję dla item2
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
