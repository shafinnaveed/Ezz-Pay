package com.example.ezzpay

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

class HomeActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var accountNumberTextView: TextView
    private lateinit var balanceTextView: TextView
    private lateinit var pointsTextView: TextView
   private lateinit var showBalance: Button
   private lateinit var hideBalance: Button
    private lateinit var generateQRCodeButton: ImageButton
    private lateinit var qrCodeImageView: ImageView
    private var accountNumber="1234567"
    private lateinit var qrTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userNameTextView = findViewById(R.id.userNameTextView)
        accountNumberTextView = findViewById(R.id.accountNumberTextView)
        balanceTextView = findViewById(R.id.balanceTextView)
        pointsTextView = findViewById(R.id.pointsTextView)
        showBalance = findViewById(R.id.showBalanceButton)
        hideBalance=findViewById(R.id.hideBalance)
        qrTextView=findViewById(R.id.qrText)
        generateQRCodeButton = findViewById<ImageButton>(R.id.qrCodeButton)
        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        generateQRCodeButton.setOnClickListener {
            qrCodeImageView.visibility = View.VISIBLE
            qrTextView.visibility=View.VISIBLE

            generateQRCode(accountNumber)
        }

        // Fetch and display user data
        fetchUserData()
    }
    override fun onBackPressed() {
        // Check if the QR code is displayed
        if (qrCodeImageView.drawable != null) {
            qrCodeImageView.visibility = View.INVISIBLE
            qrTextView.visibility=View.INVISIBLE
        } else {
            // Handle the back press as needed, maybe show a confirmation dialog
            super.onBackPressed()
        }
    }
    private fun generateQRCode(data: String) {
        try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                800,
                800
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            qrCodeImageView.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(currentUser?.uid ?: "")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userName = snapshot.child("name").value.toString()
                    accountNumber = snapshot.child("accountNumber").value.toString()
                    val balance = snapshot.child("balance").value.toString()
                    val points = snapshot.child("points").value.toString()
                    showBalance.setOnClickListener { showBalance.visibility = View.INVISIBLE
                    hideBalance.visibility=View.VISIBLE
                    balanceTextView.visibility=View.VISIBLE
                    pointsTextView.visibility=View.VISIBLE}

                    hideBalance.setOnClickListener { showBalance.visibility = View.VISIBLE
                        hideBalance.visibility=View.INVISIBLE
                        balanceTextView.visibility=View.INVISIBLE
                        pointsTextView.visibility=View.INVISIBLE}

                    // Update UI with retrieved data
                    updateUI(userName, accountNumber, balance, points)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun updateUI(userName: String, accountNumber: String, balance: String, points: String) {
        userNameTextView.text = userName
        accountNumberTextView.text = accountNumber
        balanceTextView.text = "Balance: $balance"
        pointsTextView.text = "Points: $points"
    }
}
