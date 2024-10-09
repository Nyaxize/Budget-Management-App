package com.example.projekt.Operations

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.projekt.Budgets.Budgets
import com.example.projekt.Activities.MainActivity
import com.example.projekt.R
import com.example.projekt.Activities.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NewTransaction : AppCompatActivity() {
    private lateinit var dateEditText: EditText
    private lateinit var categoryIconImageView: ImageView
    private lateinit var database: FirebaseDatabase
    private lateinit var amountEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var expenseType: RadioButton
    private lateinit var incomeType: RadioButton

    private var selectedCategory: NewCategory? = null

    private val categories = listOf(
        NewCategory("Food", R.drawable.ic_burger),
        NewCategory("Transport", R.drawable.ic_bus),
        NewCategory("Income", R.drawable.ic_money),
        // Dodaj więcej kategorii tutaj
    )
    val categoryImageToNameMap = mapOf(
        R.drawable.ic_burger to "Food",
        R.drawable.ic_bus to "Transport",
        R.drawable.ic_money to "Income",
        // Dodaj więcej przypisań tutaj
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newtransaction)
        supportActionBar?.title = "Add New Transaction"

        database = FirebaseDatabase.getInstance()

        amountEditText = findViewById(R.id.new_amount) // Id EditText dla kwoty
        descriptionEditText = findViewById(R.id.new_note) // Id EditText dla opisu
        categoryIconImageView = findViewById(R.id.category_icon)
        expenseType = findViewById(R.id.radioButton_expense)
        incomeType = findViewById(R.id.radioButton_income)

        val saveButton: Button = findViewById(R.id.add_transaction) // Id przycisku do zapisu
        saveButton.setOnClickListener {
            saveTransaction()
        }

        dateEditText = findViewById(R.id.new_date)
        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                // Formatuj datę i ustaw ją jako tekst EditText
                val selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                dateEditText.setText(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }, year, month, day)
            datePickerDialog.show()
        }
        val buttonSelectCategory: ImageView = findViewById(R.id.category_icon)
        buttonSelectCategory.setOnClickListener {
            showCategorySelector()
        }

    }
    private fun showCategorySelector() {
        val view = layoutInflater.inflate(R.layout.category_selector, null, false)
        val gridView: GridView = view.findViewById(R.id.gridView)

        gridView.adapter = CategoryAdapter(this, categories)

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)
        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
            categoryIconImageView.setImageResource(selectedCategory?.icon ?: 0)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }
    private fun checkBudgetLimits(transaction: Transaction) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val budgetsRef = FirebaseDatabase.getInstance().getReference("budgets").child(userId)

        budgetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var budgetFound = false

                for (budgetSnapshot in dataSnapshot.children) {
                    val budget = budgetSnapshot.getValue(Budgets::class.java)
                    if (budget != null && budget.category.equals(transaction.category, ignoreCase = true)) {
                        budgetFound = true

                        // Pobierz wszystkie transakcje dla danej kategorii i użytkownika
                        val transactionsRef = FirebaseDatabase.getInstance().getReference("transactions")
                        transactionsRef.orderByChild("userId").equalTo(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(transactionSnapshot: DataSnapshot) {
                                    var totalMonthlyAmount = 0.0
                                    var totalYearlyAmount = 0.0

                                    val currentMonth = LocalDate.now().monthValue
                                    val currentYear = LocalDate.now().year

                                    for (transactionSnap in transactionSnapshot.children) {
                                        val trans = transactionSnap.getValue(Transaction::class.java)
                                        if (trans != null && trans.category == transaction.category) {
                                            // Parsuj datę transakcji
                                            val transactionDate = LocalDate.parse(trans.date, DateTimeFormatter.ISO_LOCAL_DATE)

                                            // Sumuj transakcje z bieżącego miesiąca
                                            if (transactionDate.monthValue == currentMonth && transactionDate.year == currentYear) {
                                                totalMonthlyAmount += trans.amount
                                            }

                                            // Sumuj transakcje z bieżącego roku
                                            if (transactionDate.year == currentYear) {
                                                totalYearlyAmount += trans.amount
                                            }
                                        }
                                    }

                                    // Oblicz procent wykorzystania budżetu
                                    val monthlyPercentage = (totalMonthlyAmount / budget.monthlyLimit) * 100
                                    val yearlyPercentage = (totalYearlyAmount / budget.yearlyLimit) * 100

                                    // Sprawdź, czy zbliżamy się do limitów (90% lub więcej, ale nie przekroczyliśmy 100%)
                                    if (monthlyPercentage >= 90 && monthlyPercentage < 100) {
                                        sendNotification(
                                            "Monthly budget alert",
                                            "You have used ${monthlyPercentage.toInt()}% of your monthly limit for ${budget.category}."
                                        )
                                    }

                                    if (yearlyPercentage >= 90 && yearlyPercentage < 100) {
                                        sendNotification(
                                            "Yearly budget alert",
                                            "You have used ${yearlyPercentage.toInt()}% of your yearly limit for ${budget.category}."
                                        )
                                    }

                                    // Sprawdź, czy całkowita kwota przekracza limity
                                    if (totalMonthlyAmount > budget.monthlyLimit) {
                                        sendNotification(
                                            "Monthly budget limit exceeded",
                                            "Your monthly limit for ${budget.category} has been exceeded."
                                        )
                                    }

                                    if (totalYearlyAmount > budget.yearlyLimit) {
                                        sendNotification(
                                            "Yearly budget limit exceeded",
                                            "Your yearly limit for ${budget.category} has been exceeded."
                                        )
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Obsługa błędu
                                }
                            })
                    }
                }

                if (!budgetFound) {
                    Toast.makeText(
                        this@NewTransaction,
                        "No budget found for category ${transaction.category}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu
            }
        })
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

    private fun saveTransaction() {
        val amount = amountEditText.text.toString().toDoubleOrNull()
        val description = descriptionEditText.text.toString()
        val date = dateEditText.text.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(this, "You have to log in.", Toast.LENGTH_LONG).show()
            return
        }

        val userId = currentUser.uid
        if (amount == null) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val category = selectedCategory?.let {
            categoryImageToNameMap[it.icon]
        } ?: run {
            Toast.makeText(this, "Please select a category.", Toast.LENGTH_SHORT).show()
            return
        }
        val type = if (incomeType.isChecked) "income" else "expense"

        val ref = database.getReference("transactions")
        val transactionId = ref.push().key ?: return
        val transaction = Transaction(amount, category, date, description, userId, transactionId, type)
        ref.child(transactionId).setValue(transaction)
            .addOnSuccessListener {
                Toast.makeText(this, "New transaction added", Toast.LENGTH_SHORT).show()
                checkBudgetLimits(transaction)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add new transaction", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } else {
            Toast.makeText(this, "Permission to post notifications is not granted", Toast.LENGTH_SHORT).show()
        }
    }







}
