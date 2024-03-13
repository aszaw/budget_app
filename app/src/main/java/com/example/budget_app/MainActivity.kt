package com.example.budget_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData

object BudgetManager {
    val budgetLiveData: MutableLiveData<Double> = MutableLiveData(0.0)
}
class MainActivity : AppCompatActivity() {

    companion object{
        var budget: Double = 0.00
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val incomeButton = findViewById<Button>(R.id.button)
        val expenseButton = findViewById<Button>(R.id.button2)
        val editText = findViewById<TextView>(R.id.textView)

        BudgetManager.budgetLiveData.observe(this, { budget ->
            editText.setText(budget.toString())
        })

        incomeButton.setOnClickListener {
            val intent = Intent(this, IncomeActivity::class.java)
            startActivity(intent)
        }

        expenseButton.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

    }

}