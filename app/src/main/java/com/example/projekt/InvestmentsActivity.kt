package com.example.projekt

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class InvestmentsActivity : AppCompatActivity() {

    private lateinit var investmentsLayout: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investments)

        investmentsLayout = findViewById(R.id.investmentsLayout)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().reference.child("investments").child(userId)

        loadInvestments()
    }

    private fun loadInvestments() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                investmentsLayout.removeAllViews()
                for (investmentSnapshot in snapshot.children) {
                    val investment = investmentSnapshot.getValue(Investment::class.java)
                    if (investment != null) {
                        addInvestmentView(investment)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów
            }
        })
    }

    private fun addInvestmentView(investment: Investment) {
        val investmentView = TextView(this).apply {
            text = "${investment.type}: ${investment.details}"
            textSize = 16f
            setPadding(0, 8, 0, 8)
        }
        investmentsLayout.addView(investmentView)
    }

    fun addNewInvestment(view: android.view.View) {
        val intent = Intent(this, AddInvestmentsActivity::class.java)
        startActivity(intent)
    }
}
