package com.example.mapty

<<<<<<< Updated upstream
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class UtenteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.utente_activity)

        val profileFragment = UtenteProfileFragment()
        val wishlistFragment = UtenteWishlistFragment()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, profileFragment)
            commit()
        }

        val profileBtn: Button = findViewById(R.id.ProfileButton)
        val wishlistBtn: Button = findViewById(R.id.WishlistButton)

        profileBtn.setOnClickListener(){
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, profileFragment)
                commit()
            }
        }

        wishlistBtn.setOnClickListener(){
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, wishlistFragment)
                commit()
            }
        }

    }
=======
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
                    replaceFragment(UtenteProfiloFragment())
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


>>>>>>> Stashed changes
}