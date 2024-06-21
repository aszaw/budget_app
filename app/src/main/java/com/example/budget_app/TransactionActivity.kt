package com.example.budget_app

import android.os.Bundle
import android.view.SurfaceControl.Transaction
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
import java.util.Calendar

class TransactionActivity : AppCompatActivity() {
    private var transactionToEdit: BudgetTransaction? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)

        val confirmButton = findViewById<Button>(R.id.transactionButtonConfirm)
        val backButton = findViewById<Button>(R.id.transactionButtonBack)
        val switchRecurring = findViewById<Switch>(R.id.transactionRecurringSwitch)
        val switchEndDate = findViewById<Switch>(R.id.transactionEndDateSwitch)
        val recurringDays = findViewById<EditText>(R.id.transactionHowOftenInput)
        val recurringText = findViewById<TextView>(R.id.transactionHowOften)
        val endDateDay = findViewById<EditText>(R.id.transactionEndDateInput)
        val endDateText = findViewById<TextView>(R.id.transactionEndDate)

        val editText = findViewById<EditText>(R.id.transactionInputNumber)
        editText.filters = arrayOf(MainActivity.DecimalDigitsInputFilter())

        transactionToEdit = intent.getSerializableExtra("transaction") as? BudgetTransaction
        if (transactionToEdit != null) {
            // Autopopulate the forms with the data from the transaction object
            editText.setText(transactionToEdit?.amount.toString())
            switchRecurring.isChecked = transactionToEdit?.recurring ?: false
            recurringDays.setText(transactionToEdit?.days?.toString())
            // ... populate other fields ...
        }


        val transactionType = intent.getStringExtra("transactionType")
        if (transactionType == "transaction") {
            // Set up transaction-specific UI and logic
        } else if (transactionType == "expense") {
            // Set up expense-specific UI and logic
        }

        confirmButton.setOnClickListener {
            val inputText = editText.text.toString()
            if (inputText.isNotEmpty()) {

                val calendar = Calendar.getInstance()
                val currentDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                val amount = inputText.toDouble()
                val recurringDaysText = recurringDays.text.toString()
                val recurringDaysValue = if (recurringDaysText.isNotEmpty()) recurringDaysText.toInt() else 1
                val transaction: BudgetTransaction
                val transactions = MainActivity.loadTransactionsFromLocalStorage(this)?.toMutableList() ?: mutableListOf()

                if (transactionToEdit == null) {
                    when (transactionType) {
                        "income" -> {
                            transaction = BudgetTransaction(amount, amount/recurringDaysValue, true, currentDate, switchRecurring.isChecked, recurringDaysValue, currentDate)
                            BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.plus(amount / recurringDaysValue)
                            transactions.add(transaction)
                        }

                        "expense" -> {
                            transaction = BudgetTransaction(amount, amount/recurringDaysValue, false, currentDate, switchRecurring.isChecked, recurringDaysValue, currentDate)
                            BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.minus(amount / recurringDaysValue)
                            transactions.add(transaction)
                        }
                        else -> {
                            return@setOnClickListener
                        }
                    }
                } else {
                    transactions.remove(transactionToEdit!!)
                    BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.minus(transactionToEdit!!.amount)
                    transactionToEdit?.amount = inputText.toDouble()
                    transactionToEdit?.spreadOutAmount = inputText.toDouble() / recurringDaysValue
                    transactionToEdit?.recurring = switchRecurring.isChecked
                    transactionToEdit?.days = recurringDays.text.toString().toInt()
                    BudgetManager.budgetLiveData.value = BudgetManager.budgetLiveData.value?.plus(inputText.toDouble() / recurringDaysValue)
                    transactions.add(transactionToEdit!!)
                    // ... update other fields ...
                }

                // Save the updated list of transactions
                MainActivity.saveTransactionsToLocalStorage(this, transactions)

                // Save the updated budget
                MainActivity.saveBudgetToLocalStorage(this, BudgetManager.budgetLiveData.value ?: 0.0)

                finish()
            } else {
                Toast.makeText(this@TransactionActivity, "Please enter an amount", Toast.LENGTH_SHORT).show()
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
