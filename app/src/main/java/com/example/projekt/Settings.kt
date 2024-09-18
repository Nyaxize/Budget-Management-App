package com.example.projekt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth

class Settings : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var switchTheme: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.title = "Settings"

        // Load the preferences fragment
        supportFragmentManager.commit {
            replace(R.id.settings_fragment_container, SettingsFragment())
        }


        switchTheme = findViewById(R.id.switch_theme)

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)

        // Lista opcji językowych
        val languages = arrayOf("English", "Spanish", "French", "German", "Polish")
        val currencies = arrayOf("PLN", "USD", "EUR", "ARS", "GBP")

        // Tworzenie adaptera dla Spinnera
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        val adapter_currency = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Ustawianie adaptera dla Spinnera
        languageSpinner.adapter = adapter
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.Back_button -> {
                Toast.makeText(this, "You backed to main menu", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.Account -> {
                Toast.makeText(this, "You are already here", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.regular_payments -> {
                Toast.makeText(this, "You entered Regular Payments", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RegularPayments::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.Settings -> true
            R.id.Logoutmenu -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(applicationContext, Login::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

        }
    }
}
