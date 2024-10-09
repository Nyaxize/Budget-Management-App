package com.example.projekt.Activities

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.animation.Easing
import com.google.firebase.auth.FirebaseAuth
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import java.text.ParseException
import java.util.Calendar
import android.Manifest
import android.app.AlertDialog
import android.content.SharedPreferences
import com.example.projekt.Budgets.BudgetActivity
import com.example.projekt.Help.Help
import com.example.projekt.Login_SingUP.Login
import com.example.projekt.RegularPayments.RegularPayments
import com.example.projekt.Operations.NewTransaction
import com.example.projekt.Operations.RecoverTransaction
import com.example.projekt.Operations.RemoveTransaction
import com.example.projekt.R
import com.example.projekt.RateTheApp
import com.example.projekt.Settings

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var textView: TextView
    private lateinit var newtransaction: Button
    private lateinit var pieChart: PieChart
    private lateinit var database: FirebaseDatabase
    private lateinit var removetransaction: Button
    private lateinit var recovertransaction: Button
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askNotificationPermission()
        listInternalFiles()
        createNotificationChannel()

        database = FirebaseDatabase.getInstance()
        pieChart = findViewById(R.id.pieChart)

        supportActionBar?.title = "Main Menu"

        auth = FirebaseAuth.getInstance()
        newtransaction = findViewById(R.id.new_transaction)
        textView = findViewById(R.id.user_details)
        removetransaction= findViewById(R.id.remove_transaction)
        recovertransaction = findViewById(R.id.history)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val currentUser = auth.currentUser

        if (currentUser != null) {


        } else {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }
        newtransaction.setOnClickListener {
            val intent = Intent(applicationContext, NewTransaction::class.java)
            startActivity(intent)
            finish()
        }
        removetransaction.setOnClickListener {
            val intent = Intent(applicationContext, RemoveTransaction::class.java)
            startActivity(intent)
            finish()
        }
        recovertransaction.setOnClickListener {
            val intent = Intent(applicationContext, RecoverTransaction::class.java)
            startActivity(intent)
            finish()
        }


        val todayButton: Button = findViewById(R.id.today_button)
        val yearButton: Button = findViewById(R.id.year_button)
        val monthButton: Button = findViewById(R.id.month_button)
        val customButton: Button = findViewById(R.id.custom_button)

        todayButton.setOnClickListener {
            loadChartDataForToday(pieChart)
        }

        yearButton.setOnClickListener {
            loadChartDataForYear(pieChart)
        }

        monthButton.setOnClickListener {
            loadChartDataForMonth(pieChart)
        }

        customButton.setOnClickListener {
            // Wyświetl DatePickerDialog, który pozwoli użytkownikowi wybrać zakres dat
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // Tutaj możesz użyć wybranej daty (year, month, dayOfMonth) do pobrania danych
                // i zaktualizowania wykresu
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(year - 1900, month, dayOfMonth))
                // Teraz masz selectedDate w formacie "YYYY-MM-DD" i możesz użyć go do filtrowania danych
                // np. w funkcji loadChartDataForCustomRange(selectedDate, pieChart)
            }, 2022, 0, 1) // Ustaw datę początkową na styczeń 2022 (możesz dostosować do swoich potrzeb)

            datePickerDialog.show()
        }
        loadChartDataForMonth(pieChart)

    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "currency_preference") {
            loadChartDataForToday(pieChart)
        }
    }

    private fun getSelectedCurrency(): String {
        return sharedPreferences.getString("currency_preference", "PLN") ?: "PLN"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {

        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to show notifications. Would you like to grant it?")
                        .setPositiveButton("OK") { dialog, which ->
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("No thanks") { dialog, which ->
                        }
                        .show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val startDatePickerDialog = DatePickerDialog(
            this,
            { _, startYear, startMonth, startDayOfMonth ->
                val endDatePickerDialog = DatePickerDialog(
                    this,
                    { _, endYear, endMonth, endDayOfMonth ->
                        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date(startYear - 1900, startMonth, startDayOfMonth))
                        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date(endYear - 1900, endMonth, endDayOfMonth))
                        generateReport(startDate, endDate)
                    },
                    currentYear, currentMonth, currentDayOfMonth
                )
                endDatePickerDialog.show()
            },
            currentYear, currentMonth, currentDayOfMonth
        )

        startDatePickerDialog.show()
    }


    private fun generateReport(startDate: String, endDate: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User must be logged in to generate report.", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionsRef = FirebaseDatabase.getInstance().getReference("transactions")
        transactionsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("Transaction Report\n")
                    stringBuilder.append("Date Range: $startDate - $endDate\n\n")

                    for (transactionSnapshot in dataSnapshot.children) {
                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                        if (transaction != null && transaction.userId == userId && isDateInRange(transaction.date, startDate, endDate)) {
                            val amount = String.format("%.2f", transaction.amount)
                            val category = transaction.category
                            val date = transaction.date
                            val description = transaction.description

                            stringBuilder.append("Amount: $amount\n")
                            stringBuilder.append("Category: $category\n")
                            stringBuilder.append("Date: $date\n")
                            stringBuilder.append("Description: $description\n\n")
                        }
                    }

                    val report = stringBuilder.toString()
                    saveReportToFile(report)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed to load transaction data.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveReportToFile(report: String) {
        try {
            val fileName = "transaction_report.txt"
            openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(report.toByteArray())
            }
            Toast.makeText(this, "Raport transakcji został zapisany do $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Nie udało się zapisać raportu transakcji.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun loadChartDataForToday(pieChart: PieChart) {
        val today = LocalDate.now().toString()
        loadChartData(pieChart, today, today)
    }

    // Dla Month
    private fun loadChartDataForMonth(pieChart: PieChart) {
        val firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString()
        val lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).toString()
        loadChartData(pieChart, firstDayOfMonth, lastDayOfMonth)
    }

    // Dla Year
    private fun loadChartDataForYear(pieChart: PieChart) {
        val firstDayOfYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).toString()
        val lastDayOfYear = LocalDate.now().with(TemporalAdjusters.lastDayOfYear()).toString()
        loadChartData(pieChart, firstDayOfYear, lastDayOfYear)
    }


    private fun updateChart(pieChart: PieChart, entries: List<PieEntry>, totalSum: Double) {
        val selectedCurrency = getSelectedCurrency() // Pobierz wybraną walutę
        val dataSet = PieDataSet(entries, "Kategorie Wydatków")
        pieChart.apply {
            // Ustawienia wyglądu wykresu
            holeRadius = 58f
            transparentCircleRadius = 61f
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            isDrawHoleEnabled = true
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            setCenterTextSize(24f)
            setCenterTextColor(Color.BLACK) // Ustaw odpowiedni kolor

            description.isEnabled = false
            legend.isEnabled = false // Wyłącz domyślną legendę
            centerText = "$selectedCurrency${String.format("%.2f", totalSum)}" // Ustawienie tekst środkowy
        }

        dataSet.colors = ColorTemplate.createColors(ColorTemplate.MATERIAL_COLORS)
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        pieChart.data = PieData(dataSet)
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.invalidate() // Odświeżenie wykresu
    }

    private fun loadChartData(pieChart: PieChart, startDate: String, endDate: String) {
        // Przekształć milisekundy na format daty używany w bazie danych

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User must be logged in to view chart.", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionsRef = database.getReference("transactions")
        transactionsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val entries = ArrayList<PieEntry>()
                    var totalSum = 0.0
                    val categorySum = HashMap<String, Double>()

                    for (transactionSnapshot in dataSnapshot.children) {
                        val transaction = transactionSnapshot.getValue(Transaction::class.java)
                        if (transaction != null && transaction.userId == userId) {
                            val transactionDate = transaction.date
                            if (isDateInRange(transactionDate, startDate, endDate)) {
                                val amount = if (transaction.type == "expense") transaction.amount else -transaction.amount
                                val sum = categorySum.getOrDefault(transaction.category, 0.0)
                                categorySum[transaction.category] = sum + transaction.amount
                                totalSum += amount
                            }
                        }
                    }

                    // Utwórz wpisy na wykresie na podstawie categorySum i totalSum
                    entries.addAll(categorySum.map { PieEntry(it.value.toFloat(), it.key) })
                    updateChart(pieChart, entries, totalSum)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed to load transaction data.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun isDateInRange(date: String, startDate: String, endDate: String?): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val transactionDate = formatter.parse(date)
            val start = formatter.parse(startDate)
            val end = endDate?.let { formatter.parse(it) } ?: start

            val isBefore = transactionDate.before(start)
            val isAfter = transactionDate.after(end)

            // Log the results for debugging purposes
            Log.d("DateRangeCheck", "Transaction Date: $date, Start Date: $startDate, End Date: ${endDate ?: "null"}")
            Log.d("DateRangeCheck", "Is transaction date before start? $isBefore")
            Log.d("DateRangeCheck", "Is transaction date after end? $isAfter")

            return !isBefore && !isAfter
        } catch (e: ParseException) {
            Log.e("DateRangeCheck", "Error parsing dates", e)
            return false
        }
    }

    private fun listInternalFiles() {
        val filesDir = filesDir
        val fileList = filesDir.list()
        if (fileList != null) {
            for (filename in fileList) {
                Log.d("InternalFiles", "File: $filesDir/$filename")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.Back_button -> {
                Toast.makeText(this, "You are already in main menu", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.Settings -> {
                Toast.makeText(this, "You entered Settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Settings::class.java)
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
            R.id.budget -> {
                Toast.makeText(this, "You entered Budget", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, BudgetActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.Savings -> {
                Toast.makeText(this, "You entered Savings Goals", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SavingsGoalsActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.rate_the_app -> {
                Toast.makeText(this, "You entered rate the app", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RateTheApp::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.Invest -> {
                Toast.makeText(this, "You entered Investments", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, InvestmentsActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.Generate_raports -> {
                Toast.makeText(this, "You generated raport", Toast.LENGTH_SHORT).show()
                //buildTransactionRaport()
                showDatePickerDialog()
                true
            }
            R.id.Help -> {
                Toast.makeText(this, "You entered Help", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Help::class.java)
                startActivity(intent)
                finish()
                true
            }
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

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

data class Transaction(
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val description: String = "",
    val userId: String = "",
    val id: String = "",
    val type: String = ""
)

data class rubishBin(
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val description: String = "",
    val userId: String = "",
    val id: String = "",
    val type: String = ""
)
