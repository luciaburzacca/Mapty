package com.example.mapty.locale

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mapty.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class LocaleActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_locale)


        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_locale)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.frame_container_locale) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.frame_container_locale)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}