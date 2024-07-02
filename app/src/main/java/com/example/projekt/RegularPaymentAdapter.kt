package com.example.projekt

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text


class RegularPaymentAdapter(var transactions: List<Transaction>) :
    RecyclerView.Adapter<RegularPaymentAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewAmount2)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory2)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription2)
        val textViewData: TextView = itemView.findViewById(R.id.textViewData2)
        val deleteButton: Button = itemView.findViewById(R.id.buttonDelete2)
        val textViewType: TextView = itemView.findViewById(R.id.textViewType2)

        // Inne elementy UI, np. TextView z informacjami o transakcji
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.regular_payment_transaction_item, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.textViewAmount.text = transaction.amount.toString()
        holder.textViewCategory.text = transaction.category
        holder.textViewDescription.text = transaction.description
        holder.textViewData.text = transaction.date
        holder.textViewType.text = transaction.type


        holder.deleteButton.setOnClickListener {
            // Kod do usunięcia transakcji z bazy danych
            deleteTransactionColumn(holder.itemView.context, transaction.id)
        }


    }

    override fun getItemCount() = transactions.size

    private fun deleteTransactionColumn(context: Context, transactionId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Regular Payments")

        databaseRef.child(transactionId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete transaction column", Toast.LENGTH_SHORT).show()
            }
    }

}
