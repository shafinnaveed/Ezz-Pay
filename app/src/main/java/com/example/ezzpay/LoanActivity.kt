package com.example.ezzpay
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoanActivity : AppCompatActivity() {

    private lateinit var requestLoanButton: Button
    private lateinit var payBackLoanButton: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        requestLoanButton = findViewById(R.id.requestLoanButton)
        payBackLoanButton = findViewById(R.id.payBackLoanButton)

        // Initialize the database reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId ?: "")

        requestLoanButton.setOnClickListener {
            requestLoan()
        }

        payBackLoanButton.setOnClickListener {
            payBackLoan()
        }
    }

    private fun requestLoan() {
        // Check if the user has already taken a loan
        databaseReference.child("loans").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // User has not taken a loan, proceed with loan request
                    processLoanRequest()
                } else {
                    Toast.makeText(this@LoanActivity, "You have already taken a loan.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@LoanActivity, "Failed to check loan status", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processLoanRequest() {
        // Update user's balance with the loan amount
        databaseReference.child("balance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentBalance = snapshot.getValue(Double::class.java) ?: 0.0
                val loanAmount = 5000.0
                val updatedBalance = currentBalance + loanAmount

                // Update the user's balance
                databaseReference.child("balance").setValue(updatedBalance)

                // Create a new loan record in the database
                val loanRecord = mapOf(
                    "amount" to loanAmount,
                    "date" to ServerValue.TIMESTAMP
                )

                databaseReference.child("loans").setValue(loanRecord)

                Toast.makeText(this@LoanActivity, "Loan of $loanAmount granted.", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@LoanActivity, "Failed to update balance", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun payBackLoan() {
        // Check if the user has an outstanding loan
        databaseReference.child("loans").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User has an outstanding loan, proceed with repayment
                    processLoanRepayment(snapshot)
                } else {
                    Toast.makeText(this@LoanActivity, "No outstanding loan to repay.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@LoanActivity, "Failed to check loan status", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun processLoanRepayment(snapshot: DataSnapshot) {
        val loanAmount = snapshot.child("amount").getValue(Double::class.java) ?: 0.0

        // Deduct the loan amount from the user's balance
        databaseReference.child("balance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentBalance = snapshot.getValue(Double::class.java) ?: 0.0

                // Check if the user has sufficient balance to repay the loan
                if (currentBalance >= loanAmount) {
                    val updatedBalance = currentBalance - loanAmount-250

                    // Update the user's balance
                    databaseReference.child("balance").setValue(updatedBalance)

                    // Remove the loan record from the database
                    databaseReference.child("loans").removeValue()

                    Toast.makeText(this@LoanActivity, "Loan repayment successful.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoanActivity, "Insufficient balance to repay the loan.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@LoanActivity, "Failed to update balance", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
