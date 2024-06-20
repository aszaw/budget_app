package com.example.budget_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class BudgetTransaction(val amount: Double, val spreadOutAmount: Double, val isIncome: Boolean, var date: String, val recurring: Boolean, val days: Int?, var lastChecked: String?)
object BudgetManager {

    val budgetLiveData: MutableLiveData<Double> = MutableLiveData(0.00)

}
class TransactionAdapter(private var transactions: List<BudgetTransaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val recurringTextView: TextView = itemView.findViewById(R.id.recurringTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.amountTextView.text = transaction.spreadOutAmount.toString()
        holder.dateTextView.text = transaction.date
        holder.recurringTextView.text = if (transaction.recurring) "Recurring: 1 days" else "One-time"
    }

    override fun getItemCount() = transactions.size



    fun updateTransactions(newTransactions: List<BudgetTransaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

}
class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = TimeUnit.DAYS.toMillis(1) // Update every day
    private lateinit var transactions: List<BudgetTransaction>

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTransactionsAndBudget(transactions)
            handler.postDelayed(this, updateInterval)
        }
    }
    class DecimalDigitsInputFilter : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val decimalPointIndex = dest?.indexOf(".") ?: -1
            if (decimalPointIndex == -1) {
                // No decimal point yet, allow input
                return null
            }

            // Check if the text after the decimal point has more than 2 characters
            if (dend - decimalPointIndex > 2) {
                // More than 2 characters after decimal point, don't allow input
                return ""
            }

            // Allow input
            return null
        }
    }

    companion object {
        fun saveBudgetToLocalStorage(context: Context, budget: Double) {
            val sharedPreferences = context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putFloat("budget", budget.toFloat())
            editor.apply()
        }

        fun saveTransactionsToLocalStorage(context: Context, transactions: List<BudgetTransaction>) {
            val sharedPreferences = context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val transactionsJson = gson.toJson(transactions)
            Log.d("transactionSaved", transactionsJson)
            editor.putString("transactions", transactionsJson)
            editor.apply()
        }

        fun loadBudgetFromLocalStorage(context: Context): Double {
            val sharedPreferences = context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getFloat("budget", 0.0f).toDouble()
        }

        fun loadTransactionsFromLocalStorage(context: Context): List<BudgetTransaction>? {
            val sharedPreferences = context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
            val gson = Gson()
            val transactionsJson = sharedPreferences.getString("transactions", null)
            return if (transactionsJson != null) {
                val type = object : TypeToken<List<BudgetTransaction>>() {}.type
                gson.fromJson(transactionsJson, type)
            } else {
                null
            }
        }

        fun daysBetween(startDate: Date, endDate: Date): Int {
            val difference = endDate.time - startDate.time
            return (difference / (1000 * 60 * 60 * 24)).toInt()
        }

        fun parseDate(dateString: String): Date {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.parse(dateString) ?: Date()
        }

        fun formatDate(date: Date): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(date)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val incomeButton = findViewById<Button>(R.id.mainButtonIncome)
        val expenseButton = findViewById<Button>(R.id.mainButtonExpense)
        val calendarButton = findViewById<Button>(R.id.mainButtonCalendar)
        val editText = findViewById<TextView>(R.id.textView)

        var initialBudget = loadBudgetFromLocalStorage(this)
        transactions = loadTransactionsFromLocalStorage(this) ?: emptyList()

        /*
        transactions = emptyList()
        saveTransactionsToLocalStorage(this, transactions)

        saveBudgetToLocalStorage(this, 0.00)

         */




        BudgetManager.budgetLiveData.value = initialBudget

        updateTransactionsAndBudget(transactions)

        BudgetManager.budgetLiveData.observe(this) { budget ->
            editText.text = String.format("%.2f", budget)
        }

        incomeButton.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            intent.putExtra("transactionType", "income")
            startActivity(intent)
        }

        expenseButton.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            intent.putExtra("transactionType", "expense")
            startActivity(intent)
        }


        calendarButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startUpdates()
    }

    private fun startUpdates() {
        handler.postDelayed(updateRunnable, updateInterval)
    }

    private fun stopUpdates() {
        handler.removeCallbacks(updateRunnable)
    }

    override fun onResume() {
        super.onResume()
        startUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopUpdates()
    }
    private fun updateTransactionsAndBudget(transactions: List<BudgetTransaction>) {
        val currentDate = Calendar.getInstance().time

        for (transaction in transactions) {
            if (transaction.recurring == true && transaction.days != null && transaction.amount != null) {
                val lastDate = parseDate(transaction.lastChecked ?: transaction.date)
                val daysSinceLast = daysBetween(lastDate, currentDate)

                if (daysSinceLast > 0 && transaction.days != 0) {
                    val dailyAmount = transaction.amount / transaction.days

                    for (i in 1..daysSinceLast) {
                        val calendar = Calendar.getInstance()
                        calendar.time = lastDate
                        calendar.add(Calendar.DAY_OF_YEAR, i)
                        if (calendar.time.after(currentDate)) {
                            break // Stop if the date exceeds the current date
                        }

                        val amountToAdd = if (calendar.time.after(currentDate)) {
                            if (transaction.isIncome) -dailyAmount else dailyAmount
                        } else {
                            if (transaction.isIncome) dailyAmount else -dailyAmount
                        }

                        // Update the budget
                        BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.plus(amountToAdd)

                        // Log the transaction
                        val transactionType = if (amountToAdd > 0) "Added" else "Subtracted"
                        val logMessage = "$transactionType ${Math.abs(amountToAdd)} for transaction: $transaction"
                        Log.d("BudgetUpdate", logMessage)

                        transaction.lastChecked = formatDate(calendar.time)
                    }
                }
            }
        }
    }








}
