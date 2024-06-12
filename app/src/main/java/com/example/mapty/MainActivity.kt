package com.example.mapty

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }


    fun goUtenteActivity(view: View?) {
        val intent = Intent(this, UtenteActivity::class.java)
        startActivity(intent)
    }

    fun goLocaleActivity(view: View?) {
        val intent = Intent(this, LocaleActivity::class.java)
        startActivity(intent)
    }

}

