package com.orbitalsonic.inapppurchasing

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import java.io.IOException
import java.util.ArrayList

class InAppPurchase(context: Context) : PurchasesUpdatedListener{
    private val mContext: Context = context
    private var billingClient: BillingClient
    var ackPurchase =
        AcknowledgePurchaseResponseListener { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
                SharedPreferencesUtils.setPurchasedBillingValue(mContext, true)
                showMessage("Item Purchased")
                (mContext as Activity).recreate()
            }
        }

    init {
        billingClient =
            BillingClient.newBuilder(mContext).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val queryPurchase = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                    val queryPurchases = queryPurchase.purchasesList
                    if (queryPurchases != null && queryPurchases.size > 0) {
                        handlePurchases(queryPurchases)
                    } else {
                        SharedPreferencesUtils.setPurchasedBillingValue(mContext, false)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    fun productPurchase() {
        //check if service is already connected
        if (billingClient.isReady) {
            initiatePurchase()
        } else {
            billingClient =
                BillingClient.newBuilder(mContext).enablePendingPurchases().setListener(this).build()
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase()
                    } else {
                        showMessage("Error" + billingResult.debugMessage)
                    }
                }

                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    private fun initiatePurchase() {
        val skuList: MutableList<String> = ArrayList()
        skuList.add(mContext.resources.getString(R.string.product_id))
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(
            params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    billingClient.launchBillingFlow(mContext as Activity, flowParams)
                } else {
                    //try to add item/product id "purchase" inside managed product in google play console
                    showMessage("Purchase Item not Found")
                }
            } else {
                showMessage(" Error " + billingResult.debugMessage)
            }
        }
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     *
     * Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     *
     */
    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            val base64Key = mContext.getString(R.string.license_key)
            Security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

    fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            //if item is purchased
            if (mContext.getString(R.string.product_id) == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    // Invalid purchase
                    // show error to user
                   showMessage("Invalid Purchase")
                    return
                }
//                 else purchase is valid
//                if item is purchased and not acknowledged
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase)
                } else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if (!SharedPreferencesUtils.getPurchasedBillingValue(mContext)) {
                        SharedPreferencesUtils.setPurchasedBillingValue(mContext, true)
                        showMessage("Item Purchased")
                        (mContext as Activity).recreate()
                    }
                }
            } else if (mContext.getString(R.string.product_id) == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                showMessage("Purchase is Pending. Please complete Transaction")
            } else if (mContext.getString(R.string.product_id) == purchase.skus[0] && purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                SharedPreferencesUtils.setPurchasedBillingValue(mContext, false)
                showMessage("Purchase Status Unknown")
            }
        }


    }


    private fun showMessage(message: String) {
        (mContext as Activity).runOnUiThread{
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        //if item newly purchased
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult =
                billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            val alreadyPurchases = queryAlreadyPurchasesResult.purchasesList
            alreadyPurchases?.let { handlePurchases(it) }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            showMessage("Purchase Canceled")
        } else {
            showMessage("Error updated" + billingResult.debugMessage)
        }
    }


    fun onDestroyBilling() {
        billingClient.endConnection()
    }

}