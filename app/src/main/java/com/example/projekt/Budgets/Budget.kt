package com.example.projekt.Budgets

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.Login_SingUP.Login
import com.example.projekt.Activities.MainActivity
import com.example.projekt.R
import com.example.projekt.RegularPayments.RegularPayments
import com.google.firebase.auth.FirebaseAuth

class Budget : AppCompatActivity() {

    private lateinit var regularpayment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        supportActionBar?.title = "Budget"

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
}