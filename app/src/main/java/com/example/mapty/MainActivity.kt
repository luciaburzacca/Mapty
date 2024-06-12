package com.example.mapty

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


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

