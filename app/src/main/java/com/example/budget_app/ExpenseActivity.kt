package com.example.budget_app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExpenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense)

        val confirmButton = findViewById<Button>(R.id.expenseButtonConfirm)
        val backButton = findViewById<Button>(R.id.expenseButtonBack)
        val switchRecurring = findViewById<Switch>(R.id.expenseRecurringSwitch)
        val switchEndDate = findViewById<Switch>(R.id.expenseEndDateSwitch)
        val recurringDays = findViewById<EditText>(R.id.expenseHowOftenInput)
        val recurringText = findViewById<TextView>(R.id.expenseHowOften)
        val endDateDay = findViewById<EditText>(R.id.expenseEndDateInput)
        val endDateText = findViewById<TextView>(R.id.expenseEndDate)

        val editText = findViewById<EditText>(R.id.expenseInputNumber)
        editText.filters = arrayOf(MainActivity.DecimalDigitsInputFilter())

        confirmButton.setOnClickListener {
            val inputText = editText.text.toString()
            if (inputText.isNotEmpty()) {
                val incomeAmount = inputText.toDouble()
                BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.plus(incomeAmount)
                MainActivity.saveBudgetToLocalStorage(this, BudgetManager.budgetLiveData.value ?: 0.0)
                finish()
            } else {
                Toast.makeText(this@ExpenseActivity, "Please enter an income amount", Toast.LENGTH_SHORT).show()
            }
        }

        switchRecurring.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Show the views
                recurringDays.visibility = View.VISIBLE
                recurringText.visibility = View.VISIBLE
            } else {
                // Hide the views
                recurringDays.visibility = View.GONE
                recurringText.visibility = View.GONE
            }
        }

        switchEndDate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Show the views
                endDateDay.visibility = View.VISIBLE
                endDateText.visibility = View.VISIBLE
            } else {
                // Hide the views
                endDateDay.visibility = View.GONE
                endDateText.visibility = View.GONE
            }
        }


        backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
}