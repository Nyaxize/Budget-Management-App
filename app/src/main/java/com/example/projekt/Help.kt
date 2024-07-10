package com.example.projekt

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Help : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        supportActionBar?.title = "Help"

        val userManualButton: Button = findViewById(R.id.userManualButton)
        val faqButton: Button = findViewById(R.id.faqButton)

        userManualButton.setOnClickListener {
            val intent = Intent(this, UserManualActivity::class.java)
            startActivity(intent)
        }


        val attachImageButton: Button = findViewById(R.id.attach_file)
        attachImageButton.setOnClickListener {
            openFileChooser()
        }

        val sendButton: Button = findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            // Tutaj dodaj logikę wysyłania danych, w tym załączonego obrazu
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
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
            R.id.Account -> {
                Toast.makeText(this, "Your already here", Toast.LENGTH_SHORT).show()
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