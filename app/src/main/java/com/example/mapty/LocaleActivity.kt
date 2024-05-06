package com.example.mapty

<<<<<<< Updated upstream
class LocaleActivity {
=======
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class LocaleActivity : AppCompatActivity() {

    /*lateinit var nomeEvento : EditText
    lateinit var dataEvento : EditText
    lateinit var tipoEvento : EditText
    lateinit var prezzoEvento : EditText
    lateinit var descrizioneEvento : EditText
    lateinit var luogoEvento : EditText
    lateinit var contattoEvento : EditText
    lateinit var novoEvento : Button*/


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

        /*nomeEvento = findViewById(R.id.nomeevento_input)
        dataEvento = findViewById(R.id.dataevento_input)
        tipoEvento = findViewById(R.id.tipoevento_input)
        prezzoEvento = findViewById(R.id.prezzoevento_input)
        descrizioneEvento = findViewById(R.id.descrizioneevento_input)
        luogoEvento = findViewById(R.id.luogoevento_input)
        contattoEvento = findViewById(R.id.contattoevento_input)
        novoEvento = findViewById(R.id.novoevento_input)

        novoEvento.setOnClickListener {
            val nome = nomeEvento.text.toString()
            val data = dataEvento.text.toString()
            val tipo = tipoEvento.text.toString()
            val prezzo = prezzoEvento.text.toString()
            val descrizione = descrizioneEvento.text.toString()
            val luogo = luogoEvento.text.toString()
            val contatto = contattoEvento.text.toString()
            Log.i("Credentials", "Nome Evento: $nome, Data Evento: $data, Tipo Evento: $tipo, Prezzo Evento: $prezzo, DescrizioneEvento: $descrizione, Luogo Evento: $luogo, Contatto Evento: $contatto")
        }*/
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()

    }



>>>>>>> Stashed changes
}