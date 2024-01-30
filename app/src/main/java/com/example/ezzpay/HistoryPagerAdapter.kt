package com.example.ezzpay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter

class HistoryPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2 // Number of pages
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SentFragment()
            1 -> ReceivedFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}

// Create a data class to represent a transaction
data class Transaction(
    val senderName:String,
    val senderAccountNumber: String,
    val receiverName:String,
    val receiverAccountNumber: String,
    val amount: Int
)

// Create a RecyclerView adapter
class HistoryAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderNameTextView: TextView = itemView.findViewById(R.id.senderAccountNameTextView)
        val receiverName: TextView = itemView.findViewById(R.id.receiverAccountNameTextView)
        val senderAccountNumberTextView: TextView = itemView.findViewById(R.id.senderAccountNumberTextView)
        val receiverAccountNumberTextView: TextView = itemView.findViewById(R.id.receiverAccountNumberTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.senderNameTextView.text = "Sender Name: ${transaction.senderName}"
        holder.senderAccountNumberTextView.text = "Sender: ${transaction.senderAccountNumber}"
        holder.receiverName.text = "Receiver: ${transaction.receiverName}"
        holder.receiverAccountNumberTextView.text = "Receiver Name: ${transaction.receiverAccountNumber}"
        holder.amountTextView.text = "Amount: ${transaction.amount}"
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}

