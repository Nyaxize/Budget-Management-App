package com.example.projekt

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

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
        // Dodaj więcej kategorii tutaj
    )
    val categoryImageToNameMap = mapOf(
        R.drawable.ic_burger to "Food",
        R.drawable.ic_bus to "Transport",
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
        val transaction = Transaction(amount, category, date, description, userId, transactionId,type)
        ref.child(transactionId).setValue(transaction)
            .addOnSuccessListener {
                Toast.makeText(this, "New transaction added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add new transaction", Toast.LENGTH_SHORT).show()
            }
    }


}
