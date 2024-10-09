package com.example.projekt.Operations

import android.annotation.SuppressLint
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
import com.example.projekt.Activities.rubishBin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecoverTransaction : BaseActivity() {
    private lateinit var recovertransactionAdapter: RecoverTransactionAdapter
    private lateinit var RecoverRecyclerView: RecyclerView
    private lateinit var spinnerCategory: Spinner
    private lateinit var editTextStartDate: EditText
    private lateinit var editTextEndDate: EditText
    private var transactions: List<rubishBin> = listOf()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_transaction)
        supportActionBar?.title = "RubishBin"

        RecoverRecyclerView = findViewById(R.id.RecoverRecyclerView)
        RecoverRecyclerView.layoutManager = LinearLayoutManager(this)

        recovertransactionAdapter = RecoverTransactionAdapter(listOf(), this)
        RecoverRecyclerView.adapter = recovertransactionAdapter

        spinnerCategory = findViewById(R.id.spinner_category)
        editTextStartDate = findViewById(R.id.start_date)
        editTextEndDate = findViewById(R.id.end_date)

        // Inicjalizacja Spinnera z kategoriami
        val categories = listOf("Wszystkie", "Food", "Transport", "Income") // Dodaj swoje kategorie
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        setupListeners()

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

    private fun setupListeners() {
        // Listener na spinner kategorii
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listener na datę początkową
        editTextStartDate.setOnClickListener {
            showDatePickerDialog(editTextStartDate)
        }

        editTextStartDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter()
            }
        })

        // Listener na datę końcową
        editTextEndDate.setOnClickListener {
            showDatePickerDialog(editTextEndDate)
        }

        editTextEndDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter()
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

            // Sprawdź poprawność daty transakcji
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

        recovertransactionAdapter.RubishBin = filteredTransactions
        recovertransactionAdapter.notifyDataSetChanged()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDayOfMonth"
            editText.setText(selectedDate)  // Ustaw wybraną datę w polu tekstowym
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun loadTransactions() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("rubishBin")
        databaseRef.orderByChild("userId").equalTo(FirebaseAuth.getInstance().currentUser?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    transactions = mutableListOf()
                    for (transactionSnapshot in dataSnapshot.children) {
                        val transaction = transactionSnapshot.getValue(rubishBin::class.java)
                        transaction?.let { transactions = transactions + it }
                    }
                    applyFilter()  // Zastosuj filtr po załadowaniu danych
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@RecoverTransaction, "Failed to load transactions.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
