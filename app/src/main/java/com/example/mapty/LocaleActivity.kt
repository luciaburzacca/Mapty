package com.example.mapty

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class LocaleActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_locale)

        bottomNavigationView = findViewById(R.id.bottom_navigation_locale)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.bottom_home_locale -> {
                    replaceFragment(LocaleHomeFragment())
                    true
                }

                R.id.bottom_calendar -> {
                    replaceFragment(LocaleEventiFragment())
                    true
                }

                R.id.bottom_profile_locale -> {
                    replaceFragment(LocaleProfiloFragment())
                    true
                }
                else -> false

            }
        }
        replaceFragment(LocaleHomeFragment())
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()

    }
}