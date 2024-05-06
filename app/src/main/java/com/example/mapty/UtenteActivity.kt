package com.example.mapty

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UtenteActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_utente)

        bottomNavigationView = findViewById(R.id.bottom_navigation_utente)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.bottom_home -> {
                    replaceFragment(UtenteHomeFragment())
                    true
                }

                R.id.bottom_wishlist -> {
                    replaceFragment(UtenteWishlistFragment())
                    true
                }

                R.id.bottom_profile -> {
                    replaceFragment(UtenteProfileFragment())
                    true
                }
                else -> false

            }
        }
        replaceFragment(UtenteHomeFragment())
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()

    }
}