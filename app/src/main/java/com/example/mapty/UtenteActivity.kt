package com.example.mapty

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
}