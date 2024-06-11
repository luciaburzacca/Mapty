package com.example.mapty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.*
import java.util.*
class LocaleNewEventoFragment : Fragment() {

    private lateinit var etNuovoEvento: EditText
    private lateinit var etDescrizione: EditText
    private lateinit var spinnerTipoEvento: Spinner
    private lateinit var etData: EditText
    private lateinit var etOraInizio: EditText
    private lateinit var etOraFine: EditText
    private lateinit var etPrezzo: EditText
    private lateinit var etContatto: EditText
    private lateinit var btnSalva: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_locale_new_evento, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etNuovoEvento = view.findViewById(R.id.editNomeEvento)
        etDescrizione = view.findViewById(R.id.editdescrizioneEvento)
        spinnerTipoEvento = view.findViewById(R.id.spinnerTipoEvento)
        etData = view.findViewById(R.id.etData)
        etOraInizio = view.findViewById(R.id.etOraInizio)
        etOraFine = view.findViewById(R.id.etOraFine)
        etPrezzo = view.findViewById(R.id.editprezzoEvento)
        etContatto = view.findViewById(R.id.editcontattoEvento)
        //btnSalva = view.findViewById(R.id.btnSalva)

        // Popola lo Spinner con i tipi di luogo
        val tipiLuogo = arrayOf("Bar Party", "Beach Party", "Disco Party", "Films Night",
            "Karaoke", "Live Music", "Local Parties", "Raggaeton Party", "Slay Party", "Techno", "Thematic Party")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipiLuogo)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoEvento.adapter = adapter

        // Gestisci il click per selezionare la data
        etData.setOnClickListener {
            mostraDatePickerDialog()
        }

        // Gestisci il click per selezionare l'ora di inizio
        etOraInizio.setOnClickListener {
            mostraTimePickerDialog(etOraInizio)
        }

        // Gestisci il click per selezionare l'ora di fine
        etOraFine.setOnClickListener {
            mostraTimePickerDialog(etOraFine)
        }

        // Gestisci il click sul pulsante di salvataggio
        /*btnSalva.setOnClickListener {
            salvaEvento()
        }*/
    }

    private fun mostraDatePickerDialog() {
        val calendario = Calendar.getInstance()
        val anno = calendario.get(Calendar.YEAR)
        val mese = calendario.get(Calendar.MONTH)
        val giorno = calendario.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, annoSelezionato, meseSelezionato, giornoSelezionato ->
            val data = "$giornoSelezionato/${meseSelezionato + 1}/$annoSelezionato"
            etData.setText(data)
        }, anno, mese, giorno)

        datePickerDialog.show()
    }

    private fun mostraTimePickerDialog(editText: EditText) {
        val calendario = Calendar.getInstance()
        val ora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(requireContext(), { _, oraSelezionata, minutoSelezionato ->
            val ora = String.format("%02d:%02d", oraSelezionata, minutoSelezionato)
            editText.setText(ora)
        }, ora, minuto, true)

        timePickerDialog.show()
    }

    /*private fun salvaEvento() {
        val nomeEvento = etNuovoEvento.text.toString()
        val descrizione = etDescrizione.text.toString()
        val tipoLuogo = spinnerTipoEvento.selectedItem.toString()
        val data = etData.text.toString()
        val oraInizio = etOraInizio.text.toString()
        val oraFine = etOraFine.text.toString()
        val prezzo = etPrezzo.text.toString()
        val contatto = etContatto.text.toString()

        // Logica per salvare l'evento
        Toast.makeText(requireContext(), "Evento salvato: $nomeEvento", Toast.LENGTH_SHORT).show()
    }*/
}