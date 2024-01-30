package com.example.ezzpay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var currentUserAccountNumber: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sent, container, false)
        recyclerView = view.findViewById(R.id.recyclerView) // Replace with your actual RecyclerView

        // Your other initialization code for the fragment

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the current user from Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Check if a user is signed in
        if (currentUser != null) {
            val currentUserUid = currentUser.uid
            val usersReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

            // Query the "users" node to get the account number for the current user's UID
            usersReference.child(currentUserUid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    currentUserAccountNumber = dataSnapshot.child("accountNumber").getValue(String::class.java) ?: ""

                    // Now you have the current user's account number, you can use it as needed
                    Log.d("CurrentUserAccountNumber", "Account Number: $currentUserAccountNumber")

                    // Now that you have the account number, you can fetch and display the transaction history
                    fetchAndDisplayTransactionHistory()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    Log.e("FirebaseError", "Error getting user data", databaseError.toException())
                    Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // User is not signed in, handle this case accordingly
            // For example, redirect to the login screen
            Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchAndDisplayTransactionHistory() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("history")
        val transactionList = mutableListOf<Transaction>()

        // Attach a ValueEventListener to retrieve data from Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()

                for (transactionSnapshot in snapshot.children) {
                    val senderAccountNumber = transactionSnapshot.child("senderAccountNumber").getValue(String::class.java)
                    val receiverAccountNumber = transactionSnapshot.child("receiverAccountNumber").getValue(String::class.java)
                    val senderName = transactionSnapshot.child("senderName").getValue(String::class.java)
                    val receiverName = transactionSnapshot.child("receiverName").getValue(String::class.java)
                    val amount = transactionSnapshot.child("amount").getValue(Int::class.java)

                    // Check if the current user's account number is in senderAccountNumber
                    if (senderAccountNumber == currentUserAccountNumber) {
                        // Add the transaction to the list
                        val transaction = Transaction(senderName ?: "",senderAccountNumber ?: "",receiverName ?: "", receiverAccountNumber ?: "", amount ?: 0)
                        transactionList.add(transaction)
                    }
                }

                // Update RecyclerView with the new data on the main thread
                requireActivity().runOnUiThread {
                    val adapter = HistoryAdapter(transactionList)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("FirebaseError", "Failed to retrieve data", error.toException())
                Toast.makeText(requireContext(), "Failed to retrieve data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
