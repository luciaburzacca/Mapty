package com.example.mapty.utente

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapty.R

import org.osmdroid.config.Configuration





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