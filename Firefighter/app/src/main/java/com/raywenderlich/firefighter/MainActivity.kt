package com.raywenderlich.firefighter

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    private var gameStarted = false

    private lateinit var countDownTimer: CountDownTimer
    private var countDownInterval: Long = 1000
    private var timeLeft = 60
    private var score = 0

    private lateinit var gameScoreTextView: TextView
    private lateinit var timeLeftTextView: TextView
    private lateinit var tapMeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called. Score is: $score")

        gameScoreTextView = findViewById(R.id.game_score_text_view)
        timeLeftTextView = findViewById(R.id.time_left_text_view)
        tapMeButton = findViewById(R.id.tap_me_button)

        tapMeButton.setOnClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce))
            incrementScore()
        }

        if (savedInstanceState != null ){
            var restoredScore = savedInstanceState.getInt(SCORE_KEY)
            var restoredTimeLeft = savedInstanceState.getInt(TIME_LEFT_KEY)

            prepareGame(restoredTimeLeft, restoredScore)

            if (restoredTimeLeft < INITIAL_TIME_LEFT) {
                startGame()
            }
        } else {
            prepareGame(INITIAL_TIME_LEFT, INITIAL_SCORE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(SCORE_KEY, score)
        outState.putInt(TIME_LEFT_KEY, timeLeft)
        countDownTimer.cancel()

        Log.d(TAG, "onSaveInstanceState: saving score $score and time left $timeLeft")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy called")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.about_item) {
            showInfo()
        }

        return true
    }

    private fun showInfo() {
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.about_title, BuildConfig.VERSION_NAME))
            .setMessage(getString(R.string.about_message))
            .create()
            .show()
    }

    private fun incrementScore() {
        if (!gameStarted) {
            startGame()
        }

        score++

        val newScore = getString(R.string.your_score, score)
        gameScoreTextView.text = newScore
    }

    private fun prepareGame(initialTimeLeft: Int, initialScore: Int) {
        timeLeft = initialTimeLeft
        score = initialScore

        gameScoreTextView.text = getString(R.string.your_score, score)
        timeLeftTextView.text = getString(R.string.time_left, timeLeft)

        countDownTimer = object : CountDownTimer((timeLeft * 1000).toLong(), countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished.toInt() / 1000
                timeLeftTextView.text = getString(R.string.time_left, timeLeft)
            }

            override fun onFinish() {
                endGame()
            }
        }

        gameStarted = false
    }

    private fun startGame() {
        gameStarted = true
        countDownTimer.start()
    }

    private fun endGame() {
        Toast
            .makeText(this, getString(R.string.game_over_message, score), Toast.LENGTH_LONG)
            .show()

        prepareGame(INITIAL_TIME_LEFT, INITIAL_SCORE)
    }

    companion object {
        private const val INITIAL_TIME_LEFT = 60
        private const val INITIAL_SCORE = 0

        private const val SCORE_KEY = "SCORE_KEY"
        private const val TIME_LEFT_KEY = "TIME_LEFT_KEY"
    }
}
