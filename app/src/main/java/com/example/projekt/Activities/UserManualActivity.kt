package com.example.projekt.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.Login_SingUP.Login
import com.example.projekt.R
import com.example.projekt.RegularPayments.RegularPayments
import com.google.firebase.auth.FirebaseAuth
import net.cachapa.expandablelayout.ExpandableLayout

class UserManualActivity : AppCompatActivity() {
    private lateinit var expandableRejestracjaLogowanie: ExpandableLayout
    private lateinit var expandableDodawanieTransakcji: ExpandableLayout
    private lateinit var expandableEdytowanieTransakcji: ExpandableLayout
    private lateinit var expandablePrzegladanieTransakcji: ExpandableLayout
    private lateinit var expandableGenerowanieRaportu: ExpandableLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_manual)
        supportActionBar?.title = "User Manual"

        expandableRejestracjaLogowanie = findViewById(R.id.expandableRejestracjaLogowanie)
        expandableDodawanieTransakcji = findViewById(R.id.expandableDodawanieTransakcji)
        expandableEdytowanieTransakcji = findViewById(R.id.expandableEdytowanieTransakcji)
        expandablePrzegladanieTransakcji = findViewById(R.id.expandablePrzegladanieTransakcji)
        expandableGenerowanieRaportu = findViewById(R.id.expandableGenerowanieRaportu)
    }

    fun toggleRejestracjaLogowanie(view: View) {
        expandableRejestracjaLogowanie.toggle()
    }

    fun toggleDodawanieTransakcji(view: View) {
        expandableDodawanieTransakcji.toggle()
    }
    fun toggleEdytowanieTransakcji(view: View) {
        expandableEdytowanieTransakcji.toggle()
    }
    fun togglePrzegladanieTransakcji(view: View) {
        expandablePrzegladanieTransakcji.toggle()
    }
    fun toggleGenerowanieRaportu(view: View) {
        expandableGenerowanieRaportu.toggle()
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
}



