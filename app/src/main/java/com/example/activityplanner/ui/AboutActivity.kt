package com.example.activityplanner.ui


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.activityplanner.R
import com.google.android.material.appbar.MaterialToolbar
import com.example.activityplanner.ui.ToolbarUtils

class AboutActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvVersion: TextView
    private lateinit var btnRateApp: Button
    private lateinit var btnShareApp: Button
    private lateinit var btnPrivacyPolicy: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        //page Toolbar with title

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        ToolbarUtils.attach(this, toolbar, "About", showBack = true)

        initializeViews()
        setupVersionInfo()
        setupClickListeners()

    }
    private fun initializeViews() {
        tvVersion = findViewById(R.id.tvVersion)
        btnRateApp = findViewById(R.id.btnRateApp)
        btnShareApp = findViewById(R.id.btnShareApp)
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy)
    }

    private fun setupVersionInfo() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            tvVersion.text = "Version $versionName"
        } catch (e: PackageManager.NameNotFoundException) {
            tvVersion.text = "Version 1.0.0"
        }
    }

    private fun setupClickListeners() {
        btnRateApp.setOnClickListener {
            // In a real app, this would open the Play Store
            Toast.makeText(this, "Rate App feature would open Play Store", Toast.LENGTH_SHORT).show()
        }

        btnShareApp.setOnClickListener {
            shareApp()
        }

        btnPrivacyPolicy.setOnClickListener {
            // In a real app, this would open a web view or browser
            Toast.makeText(this, "Privacy Policy would open in browser", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out Activity Planner")
            putExtra(Intent.EXTRA_TEXT, "I'm using Activity Planner - a great app for managing tasks and staying productive. Download it now!")
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }




    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}