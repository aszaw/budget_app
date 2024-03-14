package com.example.budget_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData

object BudgetManager {
    val budgetLiveData: MutableLiveData<Double> = MutableLiveData(0.00)
}
class MainActivity : AppCompatActivity() {
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

        fun loadBudgetFromLocalStorage(context: Context): Double {
            val sharedPreferences = context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getFloat("budget", 0.0f).toDouble()
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

        val initialBudget = loadBudgetFromLocalStorage(this)
        BudgetManager.budgetLiveData.value = initialBudget

        BudgetManager.budgetLiveData.observe(this) { budget ->
            editText.text = String.format("%.2f", budget)
        }

        incomeButton.setOnClickListener {
            val intent = Intent(this, IncomeActivity::class.java)
            startActivity(intent)
        }

        expenseButton.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
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

    }

}