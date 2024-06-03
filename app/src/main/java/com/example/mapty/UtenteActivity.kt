package com.example.mapty

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class UtenteActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_utente)


        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_utente)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.frame_container_utente) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)


    }


}