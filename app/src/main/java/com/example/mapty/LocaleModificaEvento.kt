package com.example.mapty

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LocaleModificaEvento : Fragment() {

    // Variabili di classe
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var editNomeEvento: EditText
    private lateinit var editDescrizioneEvento: EditText
    private lateinit var spinnerTipoEvento: Spinner
    private lateinit var etData: EditText
    private lateinit var etOraInizio: EditText
    private lateinit var etOraFine: EditText
    private lateinit var editPrezzoEvento: EditText
    private lateinit var editNomeLocale: EditText
    private lateinit var editNumeroTelefono: EditText
    private lateinit var coordinateEvento: TextView
    private lateinit var buttonAnnulla: Button
    private lateinit var buttonEliminaEvento: Button
    private lateinit var buttonModifica: Button

    private lateinit var eventoId: String
    private var nomeLocaleOriginale: String = ""
    private var numeroTelefonoOriginale: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        eventoId = arguments?.getString("eventoId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locale_modifica_evento, container, false)

        // Inizializzazione dei componenti UI
        editNomeEvento = view.findViewById(R.id.editNomeEvento)
        editDescrizioneEvento = view.findViewById(R.id.editDescrizioneEvento)
        spinnerTipoEvento = view.findViewById(R.id.spinnerTipoEvento)
        etData = view.findViewById(R.id.etData)
        etOraInizio = view.findViewById(R.id.etOraInizio)
        etOraFine = view.findViewById(R.id.etOraFine)
        editPrezzoEvento = view.findViewById(R.id.editPrezzoEvento)
        coordinateEvento = view.findViewById(R.id.coordinateEvento)
        buttonAnnulla = view.findViewById(R.id.buttonAnnulla)
        buttonEliminaEvento = view.findViewById(R.id.buttonEliminaEvento)
        buttonModifica = view.findViewById(R.id.buttonModifica)

        // Impostazione del dropdown per tipo evento
        val tipiEvento = arrayOf("Bar Party", "Beach Party", "Disco Party", "Films Night",
            "Karaoke", "Live Music", "Local Parties", "Raggaeton Party", "Slay Party", "Techno", "Thematic Party")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipiEvento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoEvento.adapter = adapter

        // Recupero e popolamento dei dati dell'evento
        fetchEventDetails()

        // Gestione dei click sui bottoni e campi di testo
        buttonAnnulla.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        buttonEliminaEvento.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        buttonModifica.setOnClickListener {
            updateEvento()
        }

        coordinateEvento.setOnClickListener {
            navigateToMapSelection()
        }

        etData.setOnClickListener {
            showDatePickerDialog(etData)
        }

        etOraInizio.setOnClickListener {
            showTimePickerDialog(etOraInizio)
        }

        etOraFine.setOnClickListener {
            showTimePickerDialog(etOraFine)
        }

        return view
    }

    // Metodo per il recupero dei dettagli dell'evento da Firestore
    private fun fetchEventDetails() {
        firestore.collection("eventos").document(eventoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    editNomeEvento.setText(document.getString("nomeEvento"))
                    editDescrizioneEvento.setText(document.getString("descrizione"))
                    spinnerTipoEvento.setSelection(getSpinnerIndex(spinnerTipoEvento, document.getString("tipo")))
                    etData.setText(formatDate(document.getLong("data")))
                    etOraInizio.setText(formatTime(document.getLong("data")))
                    etOraFine.setText(formatTime(document.getLong("dataFine")))
                    editPrezzoEvento.setText(document.getString("prezzo"))
                    editNomeLocale.setText(document.getString("nomeLocale"))
                    editNumeroTelefono.setText(document.getString("numeroTelefono"))

                    // Salvataggio originale di nomeLocale e numeroTelefono
                    nomeLocaleOriginale = document.getString("nomeLocale") ?: ""
                    numeroTelefonoOriginale = document.getString("numeroTelefono") ?: ""

                    // Popolare coordinateEvento con latitudine e longitudine
                    coordinateEvento.text = "${document.getDouble("latitudine")}, ${document.getDouble("longitudine")}"

                } else {
                    Toast.makeText(context, "Evento non trovato", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore nel recupero dell'evento", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
    }

    // Metodo per l'aggiornamento dell'evento su Firestore
    private fun updateEvento() {
        val nomeEvento = editNomeEvento.text.toString().trim()
        val descrizioneEvento = editDescrizioneEvento.text.toString().trim()
        val tipoEvento = spinnerTipoEvento.selectedItem.toString()
        val data = etData.text.toString().trim()
        val oraInizio = etOraInizio.text.toString().trim()
        val oraFine = etOraFine.text.toString().trim()
        val prezzoEvento = editPrezzoEvento.text.toString().trim()
        val nomeLocale = editNomeLocale.text.toString().trim()
        val numeroTelefono = editNumeroTelefono.text.toString().trim()
        val coordinate = coordinateEvento.text.toString().trim() // Gestire latitudine e longitudine

        // Validazione dei campi di input
        if (nomeEvento.isEmpty() || descrizioneEvento.isEmpty() || data.isEmpty() || oraInizio.isEmpty() || oraFine.isEmpty() || prezzoEvento.isEmpty() || nomeLocale.isEmpty() || numeroTelefono.isEmpty() || coordinate.isEmpty()) {
            Toast.makeText(context, "Per favore, completa tutti i campi", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertire data e ora in millisecondi
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        cal.time = sdf.parse("$data $oraInizio")!!
        val dataInizio = cal.timeInMillis

        cal.time = sdf.parse("$data $oraFine")!!
        var dataFine = cal.timeInMillis

        if (dataFine < dataInizio) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            dataFine = cal.timeInMillis
        }

        // Preparare i dati aggiornati dell'evento
        val updatedEvento = hashMapOf(
            "nomeEvento" to nomeEvento,
            "descrizione" to descrizioneEvento,
            "tipo" to tipoEvento,
            "data" to dataInizio,
            "dataFine" to dataFine,
            "prezzo" to prezzoEvento.toDouble(),
            "nomeLocale" to nomeLocale,
            "numeroTelefono" to numeroTelefono,
            // Aggiungere altri campi se necessario
        )

        firestore.collection("eventos").document(eventoId)
            .set(updatedEvento)
            .addOnSuccessListener {
                Toast.makeText(context, "Evento aggiornato con successo", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore durante l'aggiornamento dell'evento", Toast.LENGTH_SHORT).show()
                Log.e("LocaleModificaEvento", "Errore durante l'aggiornamento dell'evento", e)
            }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Elimina Evento")
            .setMessage("Sei sicuro di voler eliminare questo evento?")
            .setPositiveButton("Elimina") { _, _ ->
                deleteEvento()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun deleteEvento() {
        firestore.collection("eventos").document(eventoId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Evento eliminato con successo", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore durante l'eliminazione dell'evento", Toast.LENGTH_SHORT).show()
                Log.e("LocaleModificaEvento", "Errore durante l'eliminazione dell'evento", e)
            }
    }

    private fun navigateToMapSelection() {
        findNavController().navigate(R.id.action_localeModificaEvento_to_localeSelezionaMappaFragment)
    }


    private fun showDatePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            editText.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
        }
        DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            editText.setText(String.format("%02d:%02d", hourOfDay, minute))
        }
        TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    // Metodo per formattare la data
    private fun formatDate(milliseconds: Long?): String {
        if (milliseconds == null) return ""
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        return sdf.format(calendar.time)
    }

    // Metodo per formattare l'ora
    private fun formatTime(milliseconds: Long?): String {
        if (milliseconds == null) return ""
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        return sdf.format(calendar.time)
    }

    // Metodo per ottenere l'indice dell'elemento selezionato nello Spinner
    private fun getSpinnerIndex(spinner: Spinner, item: String?): Int {
        val adapter = spinner.adapter
        if (adapter != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == item) {
                    return i
                }
            }
        }
        return 0
    }
}



