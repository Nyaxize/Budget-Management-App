package com.example.projekt.RegularPayments

import android.content.Intent
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
import com.example.projekt.Login_SingUP.Login
import com.example.projekt.Activities.MainActivity
import com.example.projekt.Operations.NewCategory
import com.example.projekt.R
import com.example.projekt.Activities.Transaction
import com.example.projekt.Operations.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegularPaymentsCreate : AppCompatActivity() {

    private lateinit var categoryIconImageView: ImageView
    private var selectedCategory: NewCategory? = null
    private lateinit var incomeType: RadioButton
    private lateinit var expenseType: RadioButton
    private lateinit var paymentname: EditText
    private lateinit var freq: EditText
    private lateinit var addauto: CheckBox
    private lateinit var amount: EditText
    private lateinit var desc: EditText
    private lateinit var database: FirebaseDatabase

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regular_payments_create)
        supportActionBar?.title = "Regular Payments Create"

        database = FirebaseDatabase.getInstance()

        incomeType = findViewById(R.id.radioButton_income)
        expenseType = findViewById(R.id.radioButton_expense)
        paymentname = findViewById(R.id.new_note)
        freq = findViewById(R.id.frequency)
        addauto = findViewById(R.id.add_auto)
        amount = findViewById(R.id.new_amount)
        desc = findViewById(R.id.description_regular_payment)
        categoryIconImageView = findViewById(R.id.category_icon)

        val buttonSelectCategory: ImageView = findViewById(R.id.category_icon)
        buttonSelectCategory.setOnClickListener {
            showCategorySelector()
        }
        val createRegularPayment: Button = findViewById(R.id.add_transaction)
        createRegularPayment.setOnClickListener{
            addRegularPayment()
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

        private fun addRegularPayment() {
            val freqtype = freq.text.toString()
            val amount2 = amount.text.toString().toDoubleOrNull()
            val desc2 = desc.text.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Toast.makeText(this, "You have to log in.", Toast.LENGTH_LONG).show()
                return
            }

            val userId = currentUser.uid
            if (amount2 == null) {
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

            val ref = database.getReference("Regular Payments")
            val regpaymenyID = ref.push().key ?: return
            val transaction = Transaction(amount2, category, freqtype, desc2, userId, regpaymenyID, type)
            ref.child(regpaymenyID).setValue(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "New transaction added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add new transaction", Toast.LENGTH_SHORT).show()
                }
        }
}