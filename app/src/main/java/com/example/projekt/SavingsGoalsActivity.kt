package com.example.projekt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SavingsGoalsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var goalsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saving_goals)

        // Inicjalizacja Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Inicjalizacja Firebase Database
        database = FirebaseDatabase.getInstance().reference
        goalsLayout = findViewById(R.id.goalsLayout)


        Log.d("SavingsGoalsActivity", "onCreate: Database reference and goalsLayout initialized")

        // Pobieranie i wyświetlanie celów oszczędnościowych
        loadGoals()
    }

    fun saveGoal(view: View) {
        val goalName = findViewById<EditText>(R.id.goalName).text.toString()
        val goalAmount = findViewById<EditText>(R.id.goalAmount).text.toString().toDouble()
        val goalDate = findViewById<EditText>(R.id.goalDate).text.toString()

        if (goalName.isEmpty() || goalAmount <= 0 || goalDate.isEmpty()) {
            Toast.makeText(this, "Please enter valid goal details", Toast.LENGTH_SHORT).show()
            return
        }

        val goalId = database.push().key ?: return
        val userId = auth.currentUser?.uid ?: return // Pobierz ID zalogowanego użytkownika

        val savingsGoal = SavingsGoal(
            goalId = goalId,
            name = goalName,
            targetAmount = goalAmount,
            savedAmount = 0.0, // Początkowo brak zaoszczędzonych środków
            endDate = goalDate,
            userId = userId // Dodane pole userId
        )

        Log.d("SavingsGoalsActivity", "saveGoal: Saving goal with ID $goalId")

        database.child("savingsGoals").child(goalId).setValue(savingsGoal)
            .addOnSuccessListener {
                Log.d("SavingsGoalsActivity", "saveGoal: Goal saved successfully")
                Toast.makeText(this, "Goal saved: $goalName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("SavingsGoalsActivity", "saveGoal: Failed to save goal", e)
                Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGoals() {
        val userId = auth.currentUser?.uid ?: return // Pobierz ID zalogowanego użytkownika

        Log.d("SavingsGoalsActivity", "loadGoals: Attempting to load goals for user $userId from Firebase")
        database.child("savingsGoals").orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("SavingsGoalsActivity", "onDataChange: Data snapshot received")
                goalsLayout.removeAllViews() // Wyczyść poprzednie cele
                if (!snapshot.exists()) {
                    Log.d("SavingsGoalsActivity", "onDataChange: No goals found")
                    Toast.makeText(this@SavingsGoalsActivity, "No goals found", Toast.LENGTH_SHORT).show()
                    showForm()
                    return
                }
                for (goalSnapshot in snapshot.children) {
                    val goal = goalSnapshot.getValue(SavingsGoal::class.java)
                    goal?.let {
                        Log.d("SavingsGoalsActivity", "onDataChange: Adding goal view for ${it.name}")
                        addGoalView(it)
                    } ?: Log.d("SavingsGoalsActivity", "onDataChange: Goal is null")
                }
                showForm()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SavingsGoalsActivity", "onCancelled: Failed to load goals", error.toException())
                showForm()
            }
        })
    }

    private fun addGoalView(goal: SavingsGoal) {
        Log.d("SavingsGoalsActivity", "addGoalView: Adding view for goal ${goal.name}")
        val view = LayoutInflater.from(this).inflate(R.layout.item_savings_goal, null)

        val goalNameText = view.findViewById<TextView>(R.id.goalNameText)
        val goalAmountText = view.findViewById<TextView>(R.id.goalAmountText)
        val goalSavedText = view.findViewById<TextView>(R.id.goalSavedText)
        val addContributionButton = view.findViewById<Button>(R.id.addContributionButton)
        val deleteGoalButton = view.findViewById<Button>(R.id.deleteGoalButton)

        goalNameText.text = goal.name
        goalAmountText.text = "Goal: ${goal.targetAmount}"
        goalSavedText.text = "Saved: ${goal.savedAmount}"

        addContributionButton.setOnClickListener {
            showContributionDialog(goal)
        }

        deleteGoalButton.setOnClickListener {
            confirmDeleteGoal(goal)
        }

        goalsLayout.addView(view)
    }

    private fun showContributionDialog(goal: SavingsGoal) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Contribution")

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, which ->
            val contribution = input.text.toString().toDoubleOrNull()
            if (contribution != null && contribution > 0) {
                updateGoalWithContribution(goal, contribution)
            } else {
                Toast.makeText(this, "Invalid contribution amount", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun updateGoalWithContribution(goal: SavingsGoal, contribution: Double) {
        val updatedSavedAmount = goal.savedAmount + contribution
        database.child("savingsGoals").child(goal.goalId).child("savedAmount").setValue(updatedSavedAmount)
            .addOnSuccessListener {
                Toast.makeText(this, "Contribution added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add contribution", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDeleteGoal(goal: SavingsGoal) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Goal")
        builder.setMessage("Are you sure you want to delete this goal?")

        builder.setPositiveButton("Yes") { dialog, which ->
            deleteGoal(goal)
        }
        builder.setNegativeButton("No") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun deleteGoal(goal: SavingsGoal) {
        database.child("savingsGoals").child(goal.goalId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Goal deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete goal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showForm() {
        findViewById<TextView>(R.id.goalNameLabel).visibility = View.VISIBLE
        findViewById<EditText>(R.id.goalName).visibility = View.VISIBLE
        findViewById<EditText>(R.id.goalAmount).visibility = View.VISIBLE
        findViewById<EditText>(R.id.goalDate).visibility = View.VISIBLE
        findViewById<Button>(R.id.saveGoalButton).visibility = View.VISIBLE
        findViewById<TextView>(R.id.savingsGoalsLabel).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.goalsLayout).visibility = View.VISIBLE
    }
}
