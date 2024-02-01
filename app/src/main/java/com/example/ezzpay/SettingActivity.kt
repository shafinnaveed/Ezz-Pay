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

    private lateinit var updateButton: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        mobileNumberEditText = findViewById(R.id.mobileNumberEditText)
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

        // Validate input
        if (newMobileNumber.isEmpty()) {
            Toast.makeText(this, "Please enter new mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        // Update mobile number if provided
        if (newMobileNumber.isNotEmpty()) {
            databaseReference.child("number").setValue(newMobileNumber)
        }

        // Update email if provided


        Toast.makeText(this, "Settings updated successfully.", Toast.LENGTH_SHORT).show()
        finish()
    }

}
