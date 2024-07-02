package com.example.projekt

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
import com.google.firebase.database.FirebaseDatabase

class RecoverTransactionAdapter(var RubishBin: List<rubishBin>, private val context: Context) :
    RecyclerView.Adapter<RecoverTransactionAdapter.RecoverViewHolder>() {

    private var currency: String? = "PLN" // Domyślna wartość

    class RecoverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewAmount2)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory2)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription2)
        val textViewData: TextView = itemView.findViewById(R.id.textViewData2)
        val textViewType: TextView = itemView.findViewById(R.id.textViewType2)
        val textViewCurrency: TextView = itemView.findViewById(R.id.textView_currency) // Używamy tego samego ID co w BaseActivity
        val recoverButton: Button = itemView.findViewById(R.id.buttonRecover)
        val deleteButton: Button = itemView.findViewById(R.id.buttonDelete2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecoverViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recover_transaction_item, parent, false)
        return RecoverViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecoverViewHolder, position: Int) {
        val RubishBin = RubishBin[position]
        holder.textViewAmount.text = RubishBin.amount.toString()
        holder.textViewCategory.text = RubishBin.category
        holder.textViewDescription.text = RubishBin.description
        holder.textViewData.text = RubishBin.date
        holder.textViewType.text = RubishBin.type

        // Ustaw walutę
        holder.textViewCurrency.text = currency

        holder.recoverButton.setOnClickListener {
            recoverTransactionToOtherTable(holder.itemView.context, RubishBin.id)
        }
        holder.deleteButton.setOnClickListener {
            deleteTransactionColumn(holder.itemView.context, RubishBin.id)
        }
    }

    override fun getItemCount() = RubishBin.size

    fun setCurrency(currency: String?) {
        this.currency = currency
        notifyDataSetChanged() // Powiadom adapter o zmianie danych
    }

    private fun recoverTransactionToOtherTable(context: Context, transactionId: String) {
        val sourceDatabaseRef = FirebaseDatabase.getInstance().getReference("rubishBin")
        val destinationDatabaseRef = FirebaseDatabase.getInstance().getReference("transactions")

        sourceDatabaseRef.child(transactionId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val recoveredData = snapshot.getValue(Transaction::class.java)

                destinationDatabaseRef.child(transactionId).setValue(recoveredData)
                    .addOnSuccessListener {
                        sourceDatabaseRef.child(transactionId).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Transaction recovered to transaction table", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to recover transaction to transaction table", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to recover transaction", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Transaction not found", Toast.LENGTH_SHORT).show()
            }
        }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch transaction data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTransactionColumn(context: Context, transactionId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("rubishBin")

        databaseRef.child(transactionId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete transaction column", Toast.LENGTH_SHORT).show()
            }
    }
}
