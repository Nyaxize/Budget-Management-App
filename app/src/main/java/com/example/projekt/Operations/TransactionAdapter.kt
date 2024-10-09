package com.example.projekt.Operations

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt.R
import com.example.projekt.Activities.Transaction
import com.google.firebase.database.FirebaseDatabase

class TransactionAdapter(var transactions: List<Transaction>, private val context: Context) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewAmount)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewData: TextView = itemView.findViewById(R.id.textViewData)
        val textViewType: TextView = itemView.findViewById(R.id.textViewType)
        val textViewCurrency: TextView = itemView.findViewById(R.id.textView_currency) // Dodaj ten wiersz
        val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)
        val editButton: Button = itemView.findViewById(R.id.buttonEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.textViewAmount.text = transaction.amount.toString()
        holder.textViewCategory.text = transaction.category
        holder.textViewDescription.text = transaction.description
        holder.textViewData.text = transaction.date
        holder.textViewType.text = transaction.type

        // Pobierz preferencje i zaktualizuj walutę
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val currency = sharedPreferences.getString("currency_preference", "PLN")
        holder.textViewCurrency.text = currency // Ustaw walutę

        holder.deleteButton.setOnClickListener {
            moveTransactionToOtherTable(holder.itemView.context, transaction.id)
        }
        holder.editButton.setOnClickListener{
            val intent = Intent(holder.itemView.context, EditTransaction::class.java)
            intent.putExtra("TRANSACTION_ID", transaction.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = transactions.size

    private fun moveTransactionToOtherTable(context: Context, transactionId: String) {
        val sourceDatabaseRef = FirebaseDatabase.getInstance().getReference("transactions")
        val destinationDatabaseRef = FirebaseDatabase.getInstance().getReference("rubishBin")

        sourceDatabaseRef.child(transactionId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val transactionData = snapshot.getValue(Transaction::class.java)
                destinationDatabaseRef.child(transactionId).setValue(transactionData)
                    .addOnSuccessListener {
                        sourceDatabaseRef.child(transactionId).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                transactions = transactions.filter { it.id != transactionId }
                                notifyDataSetChanged()
                                Toast.makeText(context, "Transaction moved to other table", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to move transaction to other table", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to move transaction to other table", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Transaction not found", Toast.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch transaction data", Toast.LENGTH_SHORT).show()
            }
    }
}
