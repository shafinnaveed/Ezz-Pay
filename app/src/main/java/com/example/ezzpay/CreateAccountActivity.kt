package com.example.ezzpay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val editTextName: EditText = findViewById(R.id.editTextName)
        val editTextNumber: EditText = findViewById(R.id.editTextNumber)
        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextIdCardNumber: EditText = findViewById(R.id.editTextIdCardNumber)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)
        val buttonCreateAccount: Button = findViewById(R.id.buttonCreateAccount)
        val editTextReEnterPassword: EditText = findViewById(R.id.editTextReEnterPassword)

        buttonCreateAccount.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val number = editTextNumber.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val idCardNumber = editTextIdCardNumber.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val reEnterPassword = editTextReEnterPassword.text.toString().trim()

            when {
                name.isEmpty() -> Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show()
                !isValidMobileNumber(number) -> Toast.makeText(this, "Please enter a valid 11-digit mobile number.", Toast.LENGTH_SHORT).show()
                email.isEmpty() -> Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                !isValidIdCardNumber(idCardNumber) -> Toast.makeText(this, "Please enter a valid 14-digit CNIC.", Toast.LENGTH_SHORT).show()
                !isValidPassword(password) -> Toast.makeText(this, "Please enter a valid password (at least 8 characters with at least one number).", Toast.LENGTH_SHORT).show()
                !passwordsMatch(password, reEnterPassword) -> Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                else -> checkUniqueIdCardNumber(idCardNumber, name, number, email, password)
            }
        }
    }

    private fun passwordsMatch(password: String, reEnterPassword: String): Boolean {
        // Check if the entered password and re-entered password match
        return password == reEnterPassword
    }
    private fun isValidMobileNumber(mobileNumber: String): Boolean {
        // Mobile number should be 11 digits
        return mobileNumber.length == 11 && mobileNumber.all { it.isDigit() }
    }

    private fun isValidIdCardNumber(idCardNumber: String): Boolean {
        // CNIC should be 14 digits
        return idCardNumber.length == 14 && idCardNumber.all { it.isDigit() }
    }

    private fun isValidPassword(password: String): Boolean {
        // Password should be at least 8 characters and contain at least one number
        return password.length >= 8 && password.any { it.isDigit() }
    }

    private fun checkUniqueIdCardNumber(idCardNumber: String, name: String, number: String, email: String, password: String) {
        // Check if the ID card number is already in the database
        val query = database.child("users").orderByChild("idCardNumber").equalTo(idCardNumber)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // ID card number is not unique
                    Toast.makeText(this@CreateAccountActivity, "ID card number is already in use. Please choose another one.", Toast.LENGTH_SHORT).show()
                } else {
                    // ID card number is unique, proceed with account creation
                    createAccount(name, number, email, idCardNumber, password)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
                Toast.makeText(this@CreateAccountActivity, "Error checking ID card number uniqueness.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createAccount(name: String, number: String, email: String, idCardNumber: String, password: String) {
        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User creation success
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Create a User object with default values
                        // Generate a unique 14-digit account number starting with "0420"
                        val accountNumber = "0420" + generateRandomDigits(10)

                        // Create a User object with default values
                        val user = User(name, number, email, idCardNumber, 0.0, 0, 5000.0, accountNumber, 0)

                        // Store user data in the database
                        database.child("users").child(userId).setValue(user)

                        Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show()

                        // Navigate to the main activity
                        navigateToMainActivity()
                    } else {
                        Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // User creation failed
                    Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun generateUniqueAccountNumber(): String {

        val randomDigits = generateRandomDigits(10)

        // Concatenate "0420" with the random digits
        val accountNumber = "0420$randomDigits"

        // Check if the account number is unique
        if (isAccountNumberUnique(accountNumber)) {
            return accountNumber
        } else {
            // If not unique, recursively generate a new account number
            return generateUniqueAccountNumber()
        }
    }

    private fun isAccountNumberUnique(accountNumber: String): Boolean {
        // Check if the account number is already in the database
        val query = database.child("users").orderByChild("accountNumber").equalTo(accountNumber)

        var isUnique = true

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                isUnique = !dataSnapshot.exists()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
                // For simplicity, let's assume it's not unique on error
                isUnique = false
            }
        })

        return isUnique
    }

    private fun generateRandomDigits(length: Int): String {
        val allowedChars = ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    private fun navigateToMainActivity() {
        // Implement the logic to navigate to the main activity
        // For example:
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish the current activity to prevent going back to it from the main activity
    }
}
