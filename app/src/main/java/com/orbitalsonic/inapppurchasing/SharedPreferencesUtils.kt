package com.orbitalsonic.inapppurchasing

import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesUtils {

    fun getPurchasedBillingValue(mContext: Context): Boolean {
        val sharedPreferences: SharedPreferences=mContext.getSharedPreferences("billingPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("billingValue", false)
    }

    fun setPurchasedBillingValue(mContext: Context,billing: Boolean) {
        val sharedPreferences: SharedPreferences=mContext.getSharedPreferences("billingPrefs", Context.MODE_PRIVATE)
        val sharedPreferencesEditor: SharedPreferences.Editor  = sharedPreferences.edit()
        sharedPreferencesEditor.putBoolean("billingValue", billing)
        sharedPreferencesEditor.apply()
    }

}