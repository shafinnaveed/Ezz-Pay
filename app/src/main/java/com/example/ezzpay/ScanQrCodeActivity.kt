package com.example.ezzpay

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanQrCodeActivity : AppCompatActivity() {

    private lateinit var scannerView: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_scan_qr_code)

        scannerView = findViewById(R.id.scannerView)
        scannerView.setAutoFocus(true)
        scannerView.setAspectTolerance(0.5f)

        IntentIntegrator(this).initiateScan()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle the result of QR code scanning
        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Handle the scanned data, for example, you can pass it to SendMoneyActivity
                val intent = Intent(this, SendMoneyActivity::class.java)
                intent.putExtra("scannedData", result.contents)
                startActivity(intent)
            } else {
                // Handle case where no QR code was scanned
                // You can display a message or take appropriate action
            }
        }
    }
}
