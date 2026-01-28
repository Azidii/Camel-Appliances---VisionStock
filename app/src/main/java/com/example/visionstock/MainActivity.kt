package com.example.visionstock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 👇 THIS LINE is what loads 'activity_main.xml'
        setContentView(R.layout.activity_main)
    }
}
