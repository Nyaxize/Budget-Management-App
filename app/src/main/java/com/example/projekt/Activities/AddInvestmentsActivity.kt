package com.example.projekt.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.Operations.Investment
import com.example.projekt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import net.cachapa.expandablelayout.ExpandableLayout

class AddInvestmentsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var stockName: EditText
    private lateinit var stockPrice: EditText
    private lateinit var stockQuantity: EditText
    private lateinit var depositBank: EditText
    private lateinit var depositAmount: EditText
    private lateinit var cryptoWallet: EditText
    private lateinit var cryptoAmount: EditText
    private lateinit var bondName: EditText
    private lateinit var bondAmount: EditText

    private lateinit var stocksSection: ExpandableLayout
    private lateinit var depositsSection: ExpandableLayout
    private lateinit var cryptoSection: ExpandableLayout
    private lateinit var bondsSection: ExpandableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_investments)
        supportActionBar?.title = "Investments"

        database = FirebaseDatabase.getInstance().reference.child("investments")

        stockName = findViewById(R.id.stockName)
        stockPrice = findViewById(R.id.stockPrice)
        stockQuantity = findViewById(R.id.stockQuantity)
        depositBank = findViewById(R.id.depositBank)
        depositAmount = findViewById(R.id.depositAmount)
        cryptoWallet = findViewById(R.id.cryptoWallet)
        cryptoAmount = findViewById(R.id.cryptoAmount)
        bondName = findViewById(R.id.bondName)
        bondAmount = findViewById(R.id.bondAmount)

        stocksSection = findViewById(R.id.stocksSection)
        depositsSection = findViewById(R.id.depositsSection)
        cryptoSection = findViewById(R.id.cryptoSection)
        bondsSection = findViewById(R.id.bondsSection)
    }

    fun toggleStocksSection(view: View) {
        stocksSection.toggle()
    }

    fun toggleDepositsSection(view: View) {
        depositsSection.toggle()
    }

    fun toggleCryptoSection(view: View) {
        cryptoSection.toggle()
    }

    fun toggleBondsSection(view: View) {
        bondsSection.toggle()
    }

    fun saveInvestment(view: View) {
        val stockNameText = stockName.text.toString()
        val stockPriceText = stockPrice.text.toString()
        val stockQuantityText = stockQuantity.text.toString()
        val depositBankText = depositBank.text.toString()
        val depositAmountText = depositAmount.text.toString()
        val cryptoWalletText = cryptoWallet.text.toString()
        val cryptoAmountText = cryptoAmount.text.toString()
        val bondNameText = bondName.text.toString()
        val bondAmountText = bondAmount.text.toString()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val investmentId = database.child(userId).push().key

        if (investmentId != null) {
            val investment = when {
                stockNameText.isNotEmpty() -> Investment(
                    type = "Stock",
                    details = "$stockNameText, $stockPriceText, $stockQuantityText",
                    userId = userId
                )
                depositBankText.isNotEmpty() -> Investment(
                    type = "Deposit",
                    details = "$depositBankText, $depositAmountText",
                    userId = userId
                )
                cryptoWalletText.isNotEmpty() -> Investment(
                    type = "Cryptocurrency",
                    details = "$cryptoWalletText, $cryptoAmountText",
                    userId = userId
                )
                bondNameText.isNotEmpty() -> Investment(
                    type = "Bond",
                    details = "$bondNameText, $bondAmountText",
                    userId = userId
                )
                else -> null
            }

            investment?.let {
                database.child(userId).child(investmentId).setValue(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Investment saved", Toast.LENGTH_SHORT).show()
                            finish() // Powrót do poprzedniej aktywności
                        } else {
                            Toast.makeText(this, "Failed to save investment", Toast.LENGTH_SHORT).show()
                        }
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
