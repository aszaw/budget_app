package com.example.budget_app

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var filteredTransactions: List<BudgetTransaction>
    private lateinit var recurringTransactions: List<BudgetTransaction>
    private lateinit var allTransactions: List<BudgetTransaction>
    private lateinit var combinedTransactions: List<BudgetTransaction>

    private var year: Int = 0
    private var month: Int = 0
    private var dayOfMonth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)

        val confirmButton = findViewById<Button>(R.id.calendarButtonConfirm)
        val backButton = findViewById<Button>(R.id.calendarButtonBack)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val recyclerView = findViewById<RecyclerView>(R.id.transactionRecycler)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load transactions from local storage
        allTransactions = MainActivity.loadTransactionsFromLocalStorage(this) ?: listOf()

        val currentDate = Calendar.getInstance()
        year = currentDate.get(Calendar.YEAR)
        month = currentDate.get(Calendar.MONTH)
        dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        recurringTransactions = allTransactions.filter { it.recurring == true }
        filteredTransactions = allTransactions.filter { it.date == "$year-${month + 1}-$dayOfMonth" && it.recurring == false }
        combinedTransactions = recurringTransactions + filteredTransactions
        transactionAdapter = TransactionAdapter(combinedTransactions)
        recyclerView.adapter = transactionAdapter


        confirmButton.setOnClickListener {
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            filterTransactionsByDate(selectedDate)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun filterTransactionsByDate(date: String) {
        filteredTransactions = allTransactions.filter { it.date == date && it.recurring == false }
        val tempRecurringTransactions = recurringTransactions.map {
            it.copy(date = date)
        }
        transactionAdapter.updateTransactions(filteredTransactions + tempRecurringTransactions)
    }


}
