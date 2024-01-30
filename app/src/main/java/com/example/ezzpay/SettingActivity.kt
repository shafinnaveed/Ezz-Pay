package com.example.ezzpay

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SettingActivity : AppCompatActivity() {

    private lateinit var mobileNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        mobileNumberEditText = findViewById(R.id.mobileNumberEditText)
        //emailEditText = findViewById(R.id.emailEditText)
        updateButton = findViewById(R.id.updateButton)

        // Initialize the database reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId ?: "")

        updateButton.setOnClickListener {
            updateSettings()
        }
    }

    private fun updateSettings() {
        val newMobileNumber = mobileNumberEditText.text.toString().trim()
        val newEmail = emailEditText.text.toString().trim()

        // Validate input
        if (newMobileNumber.isEmpty() && newEmail.isEmpty()) {
            Toast.makeText(this, "Please enter new mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        // Update mobile number if provided
        if (newMobileNumber.isNotEmpty()) {
            databaseReference.child("mobileNumber").setValue(newMobileNumber)
        }

        // Update email if provided
        if (newEmail.isNotEmpty()) {
            // Update email in Firebase Realtime Database
            databaseReference.child("email").setValue(newEmail)

            // Update email in Firebase Authentication
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.updateEmail(newEmail)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email updated successfully.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update email.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        Toast.makeText(this, "Settings updated successfully.", Toast.LENGTH_SHORT).show()
        finish()
    }

}
