package com.example.ezzpay
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.text.SimpleDateFormat
import java.util.Calendar


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
    private lateinit var complainButton: ImageButton
    private lateinit var sendMoneyButton: ImageButton
    private lateinit var historyButton: ImageButton
    private lateinit var logoutButton: ImageButton
    private lateinit var rechargeButton: ImageButton
    private lateinit var billButton:ImageButton
    private lateinit var donationButton:ImageButton
    private lateinit var loanButton: ImageButton
    private lateinit var settingButton: ImageButton
    private lateinit var moyePointsButton: ImageButton
    private lateinit var refreshButton: ImageButton



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
        sendMoneyButton=findViewById<ImageButton>(R.id.sendMoneyButton)
        complainButton=findViewById<ImageButton>(R.id.complainButton)
        historyButton=findViewById<ImageButton>(R.id.historyButton)
        logoutButton=findViewById<ImageButton>(R.id.logoutButton)
        rechargeButton=findViewById<ImageButton>(R.id.rechargeButton)
        billButton=findViewById<ImageButton>(R.id.billPaymentButton)
        donationButton=findViewById<ImageButton>(R.id.sendDonationButton)
        loanButton=findViewById<ImageButton>(R.id.loanButton)
        refreshButton=findViewById<ImageButton>(R.id.refreshButton)
        settingButton=findViewById<ImageButton>(R.id.settingsButton)
        moyePointsButton=findViewById<ImageButton>(R.id.pointsButton)

        generateQRCodeButton.setOnClickListener {
            qrCodeImageView.visibility = View.VISIBLE
            qrTextView.visibility=View.VISIBLE
            generateQRCode(accountNumber)
        }
        settingButton.setOnClickListener{
            startActivity(Intent(this, SettingActivity::class.java))
        }
        moyePointsButton.setOnClickListener{
            startActivity(Intent(this, PointsActivity::class.java))
        }

        sendMoneyButton.setOnClickListener{
            startActivity(Intent(this, SendMoneyActivity::class.java))
        }
        rechargeButton.setOnClickListener{
            startActivity(Intent(this, RechargeBillActivity::class.java))
        }
        billButton.setOnClickListener{
            startActivity(Intent(this, RechargeBillActivity::class.java))
        }
        donationButton.setOnClickListener{
            startActivity(Intent(this,DonationActivity::class.java))
        }
        loanButton.setOnClickListener{
            startActivity(Intent(this,LoanActivity::class.java))
        }
        refreshButton.setOnClickListener{
            finish()
            startActivity(Intent(this,HomeActivity::class.java))
        }


        complainButton.setOnClickListener {
            // Handle button click
            onComplainButtonClick(it)
        }


        // Fetch and display user data
        fetchUserData()

        historyButton.setOnClickListener{
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        logoutButton.setOnClickListener{
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
            finish()
            startActivity(Intent(this,MainActivity::class.java))
        }



    }
    fun onComplainButtonClick(view: View) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Complain")
        alertDialogBuilder.setMessage("Enter your complaint here:")

        // Add an EditText for user input
        val input = EditText(this)
        alertDialogBuilder.setView(input)

        // Set positive button action
        val database = Firebase.database
        val complaintsRef = database.getReference("complaints")

        alertDialogBuilder.setPositiveButton("Submit") { _, _ ->
            // Handle the submission here (send email, etc.)
            val complaintText = input.text.toString()
            val currentDateTime = getCurrentDateTime()

            // Save complaint to Firebase Realtime Database
            val currentUserAccountNo = accountNumber
            val complaintId = complaintsRef.push().key
            val complaint = Complaint(complaintId, currentUserAccountNo, complaintText,currentDateTime)

            complaintsRef.child(complaintId!!).setValue(complaint)

            // Show a confirmation message
            showConfirmationDialog()
        }

        // Set negative button action
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        // Show the AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(calendar.time)
    }
    private fun showConfirmationDialog() {
        // Create a confirmation dialog
        val confirmationDialogBuilder = AlertDialog.Builder(this)
        confirmationDialogBuilder.setTitle("Thank You")
        confirmationDialogBuilder.setMessage("Your complaint has been submitted. We will contact you within 3 working days.")

        // Set positive button action
        confirmationDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Show the confirmation dialog
        val confirmationDialog = confirmationDialogBuilder.create()
        confirmationDialog.show()

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
                1000,
                1000
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
                        refreshButton.visibility=View.VISIBLE
                    balanceTextView.visibility=View.VISIBLE
                    pointsTextView.visibility=View.VISIBLE}

                    hideBalance.setOnClickListener { showBalance.visibility = View.VISIBLE
                        hideBalance.visibility=View.INVISIBLE
                        balanceTextView.visibility=View.INVISIBLE
                        pointsTextView.visibility=View.INVISIBLE
                        refreshButton.visibility=View.INVISIBLE}

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

