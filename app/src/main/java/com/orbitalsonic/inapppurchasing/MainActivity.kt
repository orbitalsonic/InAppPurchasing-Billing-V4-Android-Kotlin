package com.orbitalsonic.inapppurchasing

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.orbitalsonic.inapppurchasing.InAppPurchase
import com.orbitalsonic.inapppurchasing.R

class MainActivity : AppCompatActivity() {

    private lateinit var btnInApp: Button
    private lateinit var btnRestorePurchasing: Button
    private lateinit var inAppPurchase: InAppPurchase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initListeners()
        inAppPurchase  = InAppPurchase(this)

    }

    private fun initViews() {
        btnInApp = findViewById(R.id.btnInApp)
        btnRestorePurchasing = findViewById(R.id.btnRestorePurchasing)

    }

    private fun initListeners() {
        btnInApp.setOnClickListener {
            inAppPurchase.productPurchase()
        }

        btnRestorePurchasing.setOnClickListener {
            showMessage("Restore Purchasing")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppPurchase.onDestroyBilling()
    }
}