package com.example.projekt

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecoverTransaction : BaseActivity() {  // Dziedziczenie z BaseActivity
    private lateinit var recovertransactionAdapter: RecoverTransactionAdapter
    private lateinit var RecoverRecyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_transaction) // Upewnij się, że to wywołanie jest poprawne
        supportActionBar?.title = "RubishBin"

        Log.d("RecoverTransaction", "Initializing views")

        RecoverRecyclerView = findViewById(R.id.RecoverRecyclerView)
        RecoverRecyclerView.layoutManager = LinearLayoutManager(this)

        // Inicjalizacja adaptera z pustą listą, która zostanie zaktualizowana po załadowaniu danych
        recovertransactionAdapter = RecoverTransactionAdapter(listOf(), this)
        RecoverRecyclerView.adapter = recovertransactionAdapter

        // Zarejestruj listenera dla preferencji
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        // Aktualizacja waluty
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        updateCurrency(sharedPreferences)
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
            R.id.Settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key == "currency_preference") {
            updateCurrency(sharedPreferences)
            recovertransactionAdapter.notifyDataSetChanged()  // Odśwież adapter po zmianie preferencji waluty
        }
    }

    override fun updateCurrency(sharedPreferences: SharedPreferences?) { // Dodajemy override
        val currency = sharedPreferences?.getString("currency_preference", "PLN")
        Log.d("RecoverTransaction", "Updating currency in RecyclerView items to: $currency")
        recovertransactionAdapter.setCurrency(currency)
    }

    private fun loadTransactions() {
        // Załaduj transakcje z bazy danych
        val databaseRef = FirebaseDatabase.getInstance().getReference("rubishBin")
        databaseRef.orderByChild("userId").equalTo(FirebaseAuth.getInstance().currentUser?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Wyczyść starą listę
                    val newTransactions = mutableListOf<rubishBin>()
                    for (transactionSnapshot in dataSnapshot.children) {
                        val transaction = transactionSnapshot.getValue(rubishBin::class.java)
                        transaction?.let { newTransactions.add(it) }
                    }
                    // Aktualizuj adapter z nową listą transakcji
                    recovertransactionAdapter.RubishBin = newTransactions
                    recovertransactionAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@RecoverTransaction, "Failed to load transactions.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
