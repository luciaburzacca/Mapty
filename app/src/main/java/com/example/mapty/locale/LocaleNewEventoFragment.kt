package com.example.mapty.locale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.InputFilter
import android.util.Log
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.example.mapty.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class LocaleNewEventoFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var editTextNomeEvento: EditText
    private lateinit var editTextDescrizioneEvento: EditText
    private lateinit var spinnerTipoEvento: Spinner
    private lateinit var editTextData: EditText
    private lateinit var editTextOraInizio: EditText
    private lateinit var editTextOraFine: EditText
    private lateinit var editTextPrezzoEvento: EditText
    private lateinit var coordinateTextView: TextView
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private lateinit var buttonAnnullaEvento: Button
    private lateinit var buttonAggiungiEvento: Button

    private val TAG = "LocaleNewEventoFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locale_new_evento, container, false)

        editTextNomeEvento = view.findViewById(R.id.editNomeEvento)
        editTextDescrizioneEvento = view.findViewById(R.id.editDescrizioneEvento)
        spinnerTipoEvento = view.findViewById(R.id.spinnerTipoEvento)
        editTextData = view.findViewById(R.id.etData)
        editTextOraInizio = view.findViewById(R.id.etOraInizio)
        editTextOraFine = view.findViewById(R.id.etOraFine)
        editTextPrezzoEvento = view.findViewById(R.id.editPrezzoEvento)
        coordinateTextView = view.findViewById(R.id.coordinateEvento)
        buttonAnnullaEvento = view.findViewById(R.id.buttonAnnullaEvento)
        buttonAggiungiEvento = view.findViewById(R.id.buttonAggiungiEvento)

        editTextData.setOnClickListener {
            showDatePickerDialog(editTextData)
        }

        editTextOraInizio.setOnClickListener {
            showTimePickerDialog(editTextOraInizio)
        }

        editTextOraFine.setOnClickListener {
            showTimePickerDialog(editTextOraFine)
        }

        buttonAnnullaEvento.setOnClickListener {
            clearFields()
        }

        buttonAggiungiEvento.setOnClickListener {
            aggiungiEvento()
        }

        coordinateTextView.setOnClickListener {
            findNavController().navigate(R.id.action_localeNewEventoFragment_to_localeSelezionaMappaFragment)
        }

        parentFragmentManager.setFragmentResultListener("location_request", viewLifecycleOwner) { _, result ->
            selectedLatitude = result.getDouble("latitude")
            selectedLongitude = result.getDouble("longitude")
            coordinateTextView.text = "Latitudine: $selectedLatitude, Longitudine: $selectedLongitude"
        }

        val tipiEvento = arrayOf("Bar Party", "Beach Party", "Disco Party", "Films Night",
            "Karaoke", "Live Music", "Local Parties", "Raggaeton Party", "Slay Party", "Techno", "Thematic Party")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipiEvento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoEvento.adapter = adapter

        editTextDescrizioneEvento.filters = arrayOf(InputFilter.LengthFilter(150))
        editTextDescrizioneEvento.setLines(3)

        return view
    }

    private fun showDatePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            editText.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
        }
        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.minDate = cal.timeInMillis
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            editText.setText(String.format("%02d:%02d", hourOfDay, minute))
        }
        TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun clearFields() {
        editTextNomeEvento.text.clear()
        editTextDescrizioneEvento.text.clear()
        editTextData.text.clear()
        editTextOraInizio.text.clear()
        editTextOraFine.text.clear()
        editTextPrezzoEvento.text.clear()
        coordinateTextView.text = ""
        spinnerTipoEvento.setSelection(0)
        selectedLatitude = null
        selectedLongitude = null
    }

    private fun aggiungiEvento() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Errore: Utente non autenticato.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Utente non autenticato")
            return
        }

        val userEmail = currentUser.email
        if (userEmail == null) {
            Toast.makeText(context, "Errore: Email utente non trovata.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Email utente non trovata")
            return
        }

        val nomeEvento = editTextNomeEvento.text.toString().trim()
        val descrizioneEvento = editTextDescrizioneEvento.text.toString().trim()
        val tipoEvento = spinnerTipoEvento.selectedItem.toString()
        val data = editTextData.text.toString().trim()
        val oraInizio = editTextOraInizio.text.toString().trim()
        val oraFine = editTextOraFine.text.toString().trim()
        val prezzoEvento = editTextPrezzoEvento.text.toString().trim()

        if (nomeEvento.isEmpty() || descrizioneEvento.isEmpty() || data.isEmpty() || oraInizio.isEmpty() || oraFine.isEmpty() || prezzoEvento.isEmpty()) {
            Toast.makeText(context, "Per favore, completa tutti i campi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedLatitude == null || selectedLongitude == null) {
            Toast.makeText(context, "Per favore, seleziona le coordinate dell'evento", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val eventDate: Date?
        try {
            eventDate = sdf.parse("$data $oraInizio")
            if (eventDate != null && eventDate.before(currentDate)) {
                Toast.makeText(context, "La data dell'evento non può essere nel passato", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: ParseException) {
            Toast.makeText(context, "Formato data o ora non valido", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = Calendar.getInstance()
        cal.time = eventDate!!
        val dataInizio = cal.timeInMillis

        cal.time = sdf.parse("$data $oraFine")!!
        var dataFine = cal.timeInMillis

        if (dataFine < dataInizio) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            dataFine = cal.timeInMillis
        }

        firestore.collection("locali").whereEqualTo("email", userEmail).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "Locale non trovato", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Locale non trovato")
                    return@addOnSuccessListener
                }

                val document = documents.firstOrNull()
                if (document == null) {
                    Toast.makeText(context, "Errore nel recupero delle informazioni del locale", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Errore nel recupero delle informazioni del locale")
                    return@addOnSuccessListener
                }

                val nomeLocale = document.getString("nomeLocale")
                val numeroTelefono = document.getString("numeroTelefono")
                val geoPoint = GeoPoint(selectedLatitude!!, selectedLongitude!!)

                if (nomeLocale != null) {
                    val evento = hashMapOf(
                        "nomeEvento" to nomeEvento,
                        "descrizione" to descrizioneEvento,
                        "tipo" to tipoEvento,
                        "data" to dataInizio,
                        "dataFine" to dataFine,
                        "prezzo" to prezzoEvento,
                        "nomeLocale" to nomeLocale,
                        "numeroTelefono" to numeroTelefono,
                        "luogo" to geoPoint
                    )

                    firestore.collection("eventos").add(evento)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Evento aggiunto con successo", Toast.LENGTH_SHORT).show()
                            clearFields()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Errore durante l'aggiunta dell'evento", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Errore durante l'aggiunta dell'evento", e)
                        }
                } else {
                    Toast.makeText(context, "Dati del locale incompleti", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Dati del locale incompleti")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Errore durante il recupero del locale", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Errore durante il recupero del locale", e)
            }
    }

}

