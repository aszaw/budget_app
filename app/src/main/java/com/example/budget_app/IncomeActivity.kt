package com.example.budget_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class IncomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_income)

        val confirmButton = findViewById<Button>(R.id.button4)
        val backButton = findViewById<Button>(R.id.button3)
        val editText = findViewById<EditText>(R.id.editTextNumberDecimal)

        confirmButton.setOnClickListener {
            val inputText = editText.text.toString()
            if (inputText.isNotEmpty()) {
                val incomeAmount = inputText.toDouble()
                MainActivity.budget += incomeAmount
                BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.plus(incomeAmount)
                finish()
            } else {
                Toast.makeText(this@IncomeActivity, "Please enter an income amount", Toast.LENGTH_SHORT).show()
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