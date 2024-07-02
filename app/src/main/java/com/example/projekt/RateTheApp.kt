package com.example.projekt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RateTheApp : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rate_the_app)
        supportActionBar?.title = "Rate The App"

        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = findViewById<EditText>(R.id.new_note)

        buttonSubmit.setOnClickListener {
            submitRatingAndReview(this, ratingBar, reviewEditText)
        }
    }

    private fun submitRatingAndReview(context: Context, ratingBar: RatingBar, reviewEditText: EditText) {
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val stars = ratingBar.rating.toInt()
        val review = reviewEditText.text.toString()

        if (stars == 0) {
            Toast.makeText(context, "Please rate the app", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("AppRatings")
        val newReviewRef = databaseRef.push()

        val reviewData = HashMap<String, Any>()
        reviewData["userId"] = userId
        reviewData["stars"] = stars
        reviewData["review"] = review

        newReviewRef.setValue(reviewData)
            .addOnSuccessListener {
                Toast.makeText(context, "Rating and review submitted successfully", Toast.LENGTH_SHORT).show()
                // Clear rating and review fields after successful submission
                ratingBar.rating = 0f
                reviewEditText.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to submit rating and review", Toast.LENGTH_SHORT).show()
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
            R.id.Account -> {
                Toast.makeText(this, "Your already here", Toast.LENGTH_SHORT).show()
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
