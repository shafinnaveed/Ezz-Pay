package com.example.ezzpay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// DonationActivity.kt
class DonationActivity : AppCompatActivity() {

    private lateinit var ngoSpinner: Spinner
    private lateinit var amountEditText: EditText
    private lateinit var donateButton: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

        ngoSpinner = findViewById(R.id.ngoSpinner)
        amountEditText = findViewById(R.id.amountEditText)
        donateButton = findViewById(R.id.donateButton)

        // Initialize the database reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId ?: "")

        donateButton.setOnClickListener {
            donate()
        }
    }

    private fun donate() {

        val donationAmount = amountEditText.text.toString().toDoubleOrNull()

        if (donationAmount != null && donationAmount > 0) {
            // Retrieve the current user's balance from the database asynchronously
            databaseReference.child("balance").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userBalance = snapshot.getValue(Double::class.java) ?: 0.0

                    if (donationAmount <= userBalance) {
                        // Process donation (subtract amount from user's balance)
                        val updatedBalance = userBalance - donationAmount

                        // Update the user's balance in the database
                        databaseReference.child("balance").setValue(updatedBalance)

                        // After processing donation, you can navigate back to the main screen or show a success message
                        Toast.makeText(this@DonationActivity, "Donation successful!", Toast.LENGTH_SHORT).show()
                        finish() // Close the donation activity
                    } else {
                        Toast.makeText(this@DonationActivity, "Insufficient balance", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Toast.makeText(this@DonationActivity, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Invalid donation amount", Toast.LENGTH_SHORT).show()
        }
    }
}