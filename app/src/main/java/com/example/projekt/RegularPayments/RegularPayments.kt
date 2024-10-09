package com.example.projekt.RegularPayments

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt.Login_SingUP.Login
import com.example.projekt.Activities.MainActivity
import com.example.projekt.R
import com.example.projekt.Activities.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegularPayments : AppCompatActivity() {
    private lateinit var transactionAdapter: RegularPaymentAdapter
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var createbutton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_transaction_regular_payments)
        supportActionBar?.title = "Regular Payments"

        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Inicjalizacja adaptera z pustą listą, która zostanie zaktualizowana po załadowaniu danych
        transactionAdapter = RegularPaymentAdapter(listOf())
        transactionsRecyclerView.adapter = transactionAdapter

        loadTransactions()

        val buttoncreate: Button = findViewById(R.id.regular_payment_button)
        buttoncreate.setOnClickListener {
            val intent = Intent(this, RegularPaymentsCreate::class.java)
            // Uruchamiamy nowe activity
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.Back_button -> {
                Toast.makeText(this, "You backed to main menu", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            R.id.regular_payments -> {
                Toast.makeText(this, "You entered Regular Payments", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RegularPayments::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.Settings -> {
                // Zrealizuj akcję dla item2
                true
            }

            R.id.Logoutmenu -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(applicationContext, Login::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun loadTransactions() {
        // Załaduj transakcje z bazy danych
        val databaseRef = FirebaseDatabase.getInstance().getReference("Regular Payments")
        databaseRef.orderByChild("userId").equalTo(FirebaseAuth.getInstance().currentUser?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Wyczyść starą listę
                    val newTransactions = mutableListOf<Transaction>()
                    for (transactionSnapshot in dataSnapshot.children) {
                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                        transaction?.let { (newTransactions ).add(it) }
                    }
                    // Aktualizuj adapter z nową listą transakcji
                    transactionAdapter.transactions = newTransactions
                    transactionAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@RegularPayments, "Failed to load transactions.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}