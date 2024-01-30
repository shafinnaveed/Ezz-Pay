package com.example.ezzpay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class SendMoneyActivity : AppCompatActivity(){

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var accountNumberEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var transferButton: Button
    private lateinit var transactionLimitText: TextView
    private lateinit var confirmButton: Button
    private var balance = "0"
    private lateinit var senderAccountNumber: String
    private lateinit var goToHomeButton: Button
    private var receiverUserName: String?="a"
    private var userName: String?="a"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money) // Your layout file
        accountNumberEditText = findViewById(R.id.accountNumberEditText)
        amountEditText = findViewById(R.id.amountEditText)
        transferButton = findViewById(R.id.transferButton)
        transactionLimitText = findViewById(R.id.transactionLimitText)
        confirmButton=findViewById(R.id.confirm_button)
        goToHomeButton=findViewById(R.id.home)
        // Initialize Firebase components
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser ?: return // Ensure user is signed in

        // Retrieve scanned data from the intent
        val scannedData = intent.getStringExtra("scannedData")
        if (!scannedData.isNullOrBlank()) {
            // Set the scanned data in the EditText
            accountNumberEditText.setText(scannedData)
        }



            val scanButton: Button = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            // Start the QR code scanning activity
            val intent = Intent(this, ScanQrCodeActivity::class.java)
            startActivity(intent)
        }
        goToHomeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }



        databaseReference =
            FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser.uid)

        // Example: Retrieve user data
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userName = dataSnapshot.child("name").value.toString()
                    balance = dataSnapshot.child("balance").value.toString()
                    senderAccountNumber = dataSnapshot.child("accountNumber").value.toString()


                    // Now you can use userName, balance, and points as needed
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
        goToHomeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        transferButton.setOnClickListener {
            // Ensure the entered amount is not empty
            val enteredAmount = amountEditText.text.toString().trim()
            if (enteredAmount.isEmpty()) {
                transactionLimitText.text = "Please enter an amount"
                showToast("Please enter an amount.")
                confirmButton.visibility= View.INVISIBLE
                return@setOnClickListener
            }

            try {
                val amount = enteredAmount.toDouble() // Convert to Double

                // Check if the user has enough balance
                if (balance.toDouble() < amount) {
                    transactionLimitText.text = "Not Enough balance"
                    showToast("Not Enough Balance.")
                    confirmButton.visibility= View.INVISIBLE
                } else {

                    initiateMoneyTransfer()
                }
            } catch (e: NumberFormatException) {
                transactionLimitText.text = "Invalid amount entered"
                showToast("Invalid amount entered")
                confirmButton.visibility= View.INVISIBLE
            }
        }

    }
    override fun onBackPressed() {
        // Handle back button press
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun initiateMoneyTransfer() {
        val enteredAccountNumber = accountNumberEditText.text.toString().trim()

        // Create a new database reference to the "users" node
        val usersReference = FirebaseDatabase.getInstance().reference.child("users")

        // Create a separate reference for the current user
        val currentUserReference = usersReference.child(firebaseUser.uid)

        // Perform a query to find the recipient with the entered account number
        usersReference.orderByChild("accountNumber").equalTo(enteredAccountNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val recipientSnapshot =
                            dataSnapshot.children.first() // Assuming there's only one matching account

                        receiverUserName = recipientSnapshot.child("name").getValue(String::class.java)
                        val recipientBalance =
                            recipientSnapshot.child("balance").getValue(Double::class.java)
                        //transferButton.setOnClickListener {

                            transactionLimitText.text = "Account Holder Name: $receiverUserName\n"
                        showToast("Account Holder Name: $receiverUserName")
                            confirmButton.visibility= View.VISIBLE
                        //}
                        confirmButton.setOnClickListener {

                            // Get the entered amount
                            val enteredAmount = amountEditText.text.toString().trim()

                            try {
                                val amount = enteredAmount.toDouble()

                                // Update the recipient's balance by adding the entered amount
                                val newRecipientBalance = recipientBalance?.plus(amount) ?: 0.0
                                recipientSnapshot.ref.child("balance").setValue(newRecipientBalance)

                                // Retrieve the sender's balance
                                val senderBalanceReference = currentUserReference.child("balance")

                                senderBalanceReference.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(senderDataSnapshot: DataSnapshot) {
                                        val senderBalance =
                                            senderDataSnapshot.getValue(Double::class.java)

                                        if (senderBalance != null) {
                                            // Subtract the transferred amount from the sender's balance
                                            val newSenderBalance = senderBalance - amount
                                            currentUserReference.child("balance")
                                                .setValue(newSenderBalance)

                                            // Display a success message
                                            transactionLimitText.text =
                                                "Money transferred successfully to $userName. New balance: $newSenderBalance"
                                            showToast("Money transferred successfully")
                                            transferMoney(senderAccountNumber, enteredAccountNumber, amount)
                                            confirmButton.visibility= View.INVISIBLE
                                            if (amount >= 1000) {
                                                // Calculate the number of points to add (1 point for every 1000)
                                                val pointsToAdd = (amount / 1000).toInt()

                                                // Retrieve the current user's points
                                                val currentUserPointsReference = currentUserReference.child("points")

                                                // Update the user's points
                                                currentUserPointsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(pointsDataSnapshot: DataSnapshot) {
                                                        val currentPoints = pointsDataSnapshot.getValue(Int::class.java) ?: 0
                                                        val newPoints = currentPoints + pointsToAdd

                                                        // Update the user's points in the database
                                                        currentUserPointsReference.setValue(newPoints)

                                                        // Display a message indicating points added
                                                        transactionLimitText.text =
                                                            "Money transferred successfully. New balance: $newSenderBalance. Points added: $pointsToAdd"
                                                        confirmButton.visibility= View.INVISIBLE
                                                    }

                                                    override fun onCancelled(pointsDatabaseError: DatabaseError) {
                                                        // Handle errors retrieving user's points
                                                        transactionLimitText.text = "Database error: ${pointsDatabaseError.message}"
                                                        confirmButton.visibility= View.INVISIBLE
                                                    }
                                                })
                                            }
                                        } else {
                                            // Handle the case where the sender's balance couldn't be retrieved
                                            transactionLimitText.text =
                                                "Error retrieving sender balance"
                                            showToast("Error retrieving sender balance")
                                            confirmButton.visibility= View.INVISIBLE
                                        }
                                    }

                                    override fun onCancelled(senderDatabaseError: DatabaseError) {
                                        // Handle errors retrieving sender's balance
                                        transactionLimitText.text =
                                            "Database error: ${senderDatabaseError.message}"
                                        confirmButton.visibility= View.INVISIBLE

                                    }
                                })

                            } catch (e: NumberFormatException) {
                                // Handle invalid amount entered
                                transactionLimitText.text = "Invalid amount entered"
                                showToast("Invalid amount entered")
                                confirmButton.visibility= View.INVISIBLE

                            }
                        }
                    } else {
                        // Account not found, show an appropriate message
                        transactionLimitText.text = "Recipient account not found"
                        showToast("Account not found")
                        confirmButton.visibility= View.INVISIBLE

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors here
                    transactionLimitText.text = "Database error: ${databaseError.message}"
                    confirmButton.visibility= View.INVISIBLE

                }
            })
    }
    private fun transferMoney(senderAccountNumber: String, receiverAccountNumber: String, amount: Double) {
        // Create a new database reference to the "history" node
        val historyReference = FirebaseDatabase.getInstance().reference.child("history")

        // Get the current timestamp to use as a unique key for the transaction
        val timestamp = ServerValue.TIMESTAMP

        // Create a map with the transaction details
        val transactionDetails = mapOf(

            "senderName" to userName,
            "senderAccountNumber" to senderAccountNumber,
            "receiverName" to receiverUserName,
            "receiverAccountNumber" to receiverAccountNumber,
            "amount" to amount,
            "timestamp" to timestamp
        )

        // Push the transaction details to the "history" node
        historyReference.push().setValue(transactionDetails)
            .addOnSuccessListener {
                // Transaction details added successfully
                println("Transaction details added successfully")
            }
            .addOnFailureListener { error ->
                // Handle errors during the transaction details addition
                println("Error adding transaction details: ${error.message}")
            }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}



