package com.example.budget_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

        val confirmButton = findViewById<Button>(R.id.button6)
        val backButton = findViewById<Button>(R.id.button5)
        val editText = findViewById<EditText>(R.id.editTextNumberDecimal2)

        confirmButton.setOnClickListener {
            val inputText = editText.text.toString()
            if (inputText.isNotEmpty()) {
                val expenseAmount = inputText.toDouble()
                MainActivity.budget -= expenseAmount
                BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.minus(expenseAmount)
                finish()
            } else {
                Toast.makeText(this@ExpenseActivity, "Please enter an expense amount", Toast.LENGTH_SHORT).show()
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