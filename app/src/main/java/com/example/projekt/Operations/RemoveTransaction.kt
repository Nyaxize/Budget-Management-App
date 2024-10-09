package com.example.projekt.Operations

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt.Activities.MainActivity
import com.example.projekt.R
import com.example.projekt.Activities.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RemoveTransaction : BaseActivity() {
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var spinnerCategory: Spinner
    private lateinit var editTextStartDate: EditText
    private lateinit var editTextEndDate: EditText
    private var transactions: List<Transaction> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_transaction)
        supportActionBar?.title = "Manage Transaction"

        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)

        transactionAdapter = TransactionAdapter(listOf(), this)
        transactionsRecyclerView.adapter = transactionAdapter

        // Inicjalizacja widoków do filtrowania
        spinnerCategory = findViewById(R.id.spinner_category)
        editTextStartDate = findViewById(R.id.start_date)
        editTextEndDate = findViewById(R.id.end_date)

        // Inicjalizacja Spinnera z kategoriami
        val categories = listOf("Wszystkie", "Food", "Transport", "Income") // Dodaj swoje kategorie
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Nasłuchiwacz zmian dla spinnera (kategorii)
        val categorySpinner: Spinner = findViewById(R.id.spinner_category)
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilter()  // Zastosuj filtr po zmianie kategorii
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nic nie robić
            }
        }

        // Nasłuchiwacz zmian dla daty początkowej
        val startDateEditText: EditText = findViewById(R.id.start_date)
        startDateEditText.setOnClickListener {
            showDatePickerDialog(startDateEditText)
        }
        startDateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter()  // Zastosuj filtr po zmianie daty początkowej
            }
        })

        // Nasłuchiwacz zmian dla daty końcowej
        val endDateEditText: EditText = findViewById(R.id.end_date)
        endDateEditText.setOnClickListener {
            showDatePickerDialog(endDateEditText)
        }
        endDateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter()  // Zastosuj filtr po zmianie daty końcowej
            }
        })

        loadTransactions()
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
            R.id.Settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadTransactions() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("transactions")
        databaseRef.orderByChild("userId").equalTo(FirebaseAuth.getInstance().currentUser?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newTransactions = mutableListOf<Transaction>()
                    for (transactionSnapshot in dataSnapshot.children) {
                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                        transaction?.let { newTransactions.add(it) }
                    }
                    transactions = newTransactions
                    transactionAdapter.transactions = newTransactions
                    transactionAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@RemoveTransaction, "Failed to load transactions.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applyFilter() {
        val selectedCategory = spinnerCategory.selectedItem?.toString() ?: "Wszystkie"
        val startDate = editTextStartDate.text.toString()
        val endDate = editTextEndDate.text.toString()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val filteredTransactions = transactions.filter { transaction ->
            val matchesCategory = selectedCategory == "Wszystkie" || transaction.category == selectedCategory


            try {
                val transactionDate = dateFormat.parse(transaction.date)

                val matchesStartDate = if (startDate.isNotEmpty()) {
                    val start = dateFormat.parse(startDate)
                    transactionDate != null && !transactionDate.before(start)
                } else {
                    true
                }

                val matchesEndDate = if (endDate.isNotEmpty()) {
                    val end = dateFormat.parse(endDate)
                    transactionDate != null && !transactionDate.after(end)
                } else {
                    true
                }

                matchesCategory && matchesStartDate && matchesEndDate
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        transactionAdapter.transactions = filteredTransactions
        transactionAdapter.notifyDataSetChanged()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            editText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }
}