package com.example.projekt.Help

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.Activities.UserManualActivity
import com.example.projekt.Login_SingUP.Login
import com.example.projekt.Activities.MainActivity
import com.example.projekt.R
import com.example.projekt.RegularPayments.RegularPayments
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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
            val emailEditText: EditText = findViewById(R.id.emailEditText) //
            val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)

            val email = emailEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (email.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Opcjonalnie: Wgraj obraz i uzyskaj jego URL
            val imageUrl = "" // Zastąp odpowiednim URL, jeśli przesyłasz obraz
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: "unknown_user"

            val helpRequest = HelpRequest(email, description, imageUrl, userId)

            // Zapisywanie do Firebase Database
            val database = FirebaseDatabase.getInstance().getReference("help_requests")
            val requestId = database.push().key

            if (requestId != null) {
                database.child(requestId).setValue(helpRequest).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Help request sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send help request", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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