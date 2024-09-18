package com.example.projekt

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var budgetsLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var AddBudgetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        supportActionBar?.title = "Budgets"

        AddBudgetButton = findViewById(R.id.addNewBudgetButton)
        budgetsLayout = findViewById(R.id.budgetsLayout)
        database = FirebaseDatabase.getInstance().reference.child("budgets")

        loadBudgets()

        AddBudgetButton.setOnClickListener {
            val intent = Intent(applicationContext, AddBudgetActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadBudgets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                budgetsLayout.removeAllViews()
                for (budgetSnapshot in snapshot.children) {
                    val budget = budgetSnapshot.getValue(Budgets::class.java)
                    val budgetId = budgetSnapshot.key
                    if (budget != null && budgetId != null) {
                        addBudgetView(budget, budgetId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu
            }
        })
    }

    private fun deleteBudget(budgetId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child(userId).child(budgetId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("BudgetActivity", "Budget deleted: $budgetId")
                // Opcjonalnie wyświetl komunikat o powodzeniu
                Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show()
                // Odśwież widok budżetów
                loadBudgets()
            } else {
                Log.e("BudgetActivity", "Failed to delete budget: $budgetId")
                // Obsługa błędu
                Toast.makeText(this, "Failed to delete budget", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addBudgetView(budget: Budgets, budgetId: String) {
        val budgetContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val nameCategoryTextView = TextView(this).apply {
            text = "${budget.name} (${budget.category}):"
            textSize = 18f
            setPadding(0, 0, 0, 8)
            setTypeface(null, Typeface.BOLD)
        }

        val monthlyLimitTextView = TextView(this).apply {
            text = "Monthly Limit: ${budget.monthlyLimit}"
            textSize = 16f
            setPadding(0, 0, 0, 4)
        }

        val yearlyLimitTextView = TextView(this).apply {
            text = "Yearly Limit: ${budget.yearlyLimit}"
            textSize = 16f
            setPadding(0, 0, 0, 4)
        }

        val deleteButton = Button(this).apply {
            text = "DELETE"
            setOnClickListener {
                deleteBudget(budgetId)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                setMargins(16, 0, 0, 0)
            }
        }

        textContainer.addView(nameCategoryTextView)
        textContainer.addView(monthlyLimitTextView)
        textContainer.addView(yearlyLimitTextView)

        budgetContainer.addView(textContainer)
        budgetContainer.addView(deleteButton)

        budgetsLayout.addView(budgetContainer)
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
            R.id.Account -> {
                Toast.makeText(this, "You entered Account", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AccountManagement::class.java)
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
