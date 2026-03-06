package com.lovetest.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilSecondName: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etSecondName: TextInputEditText

    private lateinit var btnCalculate: MaterialButton
    private lateinit var btnReset: MaterialButton
    private lateinit var btnShare: MaterialButton

    private lateinit var resultCard: View
    private lateinit var tvPercentage: TextView
    private lateinit var tvMessage: TextView
    private lateinit var progressLove: LinearProgressIndicator

    private var lastPercentage: Int? = null
    private var lastMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupActions()
    }

    private fun bindViews() {
        tilFirstName = findViewById(R.id.tilFirstName)
        tilSecondName = findViewById(R.id.tilSecondName)
        etFirstName = findViewById(R.id.etFirstName)
        etSecondName = findViewById(R.id.etSecondName)

        btnCalculate = findViewById(R.id.btnCalculate)
        btnReset = findViewById(R.id.btnReset)
        btnShare = findViewById(R.id.btnShare)

        resultCard = findViewById(R.id.resultCard)
        tvPercentage = findViewById(R.id.tvPercentage)
        tvMessage = findViewById(R.id.tvMessage)
        progressLove = findViewById(R.id.progressLove)
    }

    private fun setupActions() {
        btnCalculate.setOnClickListener {
            calculateResult()
        }

        btnReset.setOnClickListener {
            resetForm()
        }

        btnShare.setOnClickListener {
            shareResult()
        }
    }

    private fun calculateResult() {
        tilFirstName.error = null
        tilSecondName.error = null

        val firstName = etFirstName.text?.toString()?.trim().orEmpty()
        val secondName = etSecondName.text?.toString()?.trim().orEmpty()

        var isValid = true

        if (firstName.isBlank()) {
            tilFirstName.error = "Enter the first name"
            isValid = false
        }

        if (secondName.isBlank()) {
            tilSecondName.error = "Enter the second name"
            isValid = false
        }

        if (!isValid) return

        val percentage = calculateLovePercentage(firstName, secondName)
        val message = getLoveMessage(percentage)

        lastPercentage = percentage
        lastMessage = message

        tvPercentage.text = "$percentage%"
        tvMessage.text = message
        progressLove.setProgressCompat(percentage, true)

        if (resultCard.visibility != View.VISIBLE) {
            resultCard.alpha = 0f
            resultCard.visibility = View.VISIBLE
            resultCard.animate().alpha(1f).setDuration(250).start()
        }
    }

    private fun resetForm() {
        etFirstName.setText("")
        etSecondName.setText("")
        tilFirstName.error = null
        tilSecondName.error = null

        lastPercentage = null
        lastMessage = null

        progressLove.progress = 0
        resultCard.visibility = View.GONE
    }

    private fun shareResult() {
        val firstName = etFirstName.text?.toString()?.trim().orEmpty()
        val secondName = etSecondName.text?.toString()?.trim().orEmpty()
        val percentage = lastPercentage
        val message = lastMessage

        if (percentage == null || message == null) {
            Toast.makeText(this, "Calculate a result first", Toast.LENGTH_SHORT).show()
            return
        }

        val shareText = """
            Love Test Result ❤️
            
            $firstName + $secondName = $percentage%
            $message
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(intent, "Share result"))
    }

    private fun calculateLovePercentage(name1: String, name2: String): Int {
        val clean1 = normalize(name1)
        val clean2 = normalize(name2)

        val combined = listOf(clean1, clean2)
            .sorted()
            .joinToString("|")

        var hash = 7L
        for (char in combined) {
            hash = hash * 31 + char.code
        }

        val positiveHash = kotlin.math.abs(hash)

        // Playful MVP range: 40..100
        return (40 + (positiveHash % 61)).toInt()
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
    }

    private fun getLoveMessage(score: Int): String {
        return when (score) {
            in 90..100 -> "Amazing match! You two are shining together."
            in 80..89 -> "Very strong connection. Great vibes!"
            in 70..79 -> "Nice chemistry. This looks promising."
            in 60..69 -> "Good match. There is real potential here."
            in 50..59 -> "Interesting match. Keep discovering each other."
            else -> "A fun result! Sometimes opposites still attract."
        }
    }
}