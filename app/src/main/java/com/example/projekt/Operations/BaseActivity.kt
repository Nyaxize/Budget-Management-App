package com.example.projekt.Operations

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.projekt.R

abstract class BaseActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d("BaseActivity", "Preference changed: $key")
        if (key == "currency_preference") {
            updateCurrency(sharedPreferences)
        }
    }

    protected open fun updateCurrency(sharedPreferences: SharedPreferences?) {
        val currency = sharedPreferences?.getString("currency_preference", "PLN")
        val currencyTextView = findViewById<TextView>(R.id.textView_currency)
        if (currencyTextView != null) {
            Log.d("BaseActivity", "Updating currency TextView to: $currency")
            currencyTextView.text = currency
        } else {
            Log.d("BaseActivity", "currencyTextView is null")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
