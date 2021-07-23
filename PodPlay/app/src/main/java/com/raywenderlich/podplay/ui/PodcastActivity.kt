package com.raywenderlich.podplay.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.raywenderlich.podplay.R

class PodcastActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)
    }
}