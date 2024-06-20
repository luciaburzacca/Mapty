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
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class LocaleModificaEvento : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var editNomeEvento: EditText
    private lateinit var editDescrizioneEvento: EditText
    private lateinit var spinnerTipoEvento: Spinner
    private lateinit var etData: EditText
    private lateinit var etOraInizio: EditText
    private lateinit var etOraFine: EditText
    private lateinit var editPrezzoEvento: EditText

    private lateinit var coordinateEvento: TextView
    private lateinit var buttonAnnulla: Button
    private lateinit var buttonEliminaEvento: Button
    private lateinit var buttonModifica: Button

    private lateinit var eventoId: String
    private var nomeLocale: String = ""
    private var numeroTelefono: String = ""
    private var latitudine: Double? = null
    private var longitudine: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        eventoId = arguments?.getString("eventoId") ?: ""

        setFragmentResultListener("location_request") { requestKey, bundle ->
            latitudine = bundle.getDouble("latitude")
            longitudine = bundle.getDouble("longitude")
            "Latitudine: $latitudine, Longitudine: $longitudine".also { coordinateEvento.text = it }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locale_modifica_evento, container, false)

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

        val tipiEvento = arrayOf("Bar Party", "Beach Party", "Disco Party", "Films Night",
            "Karaoke", "Live Music", "Local Parties", "Raggaeton Party", "Slay Party", "Techno", "Thematic Party")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipiEvento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoEvento.adapter = adapter

        fetchEventDetails()

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
            val bundle = Bundle().apply {
                putDouble("latitudine", latitudine ?: 0.0)
                putDouble("longitudine", longitudine ?: 0.0)
            }
            findNavController().navigate(R.id.action_localeModificaEvento_to_localeSelezionaMappaFragment, bundle)
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

                    nomeLocale = document.getString("nomeLocale") ?: ""
                    numeroTelefono = document.getString("numeroTelefono") ?: ""

                    val geoPoint = document.getGeoPoint("location")
                    if (geoPoint != null) {
                        latitudine = geoPoint.latitude
                        longitudine = geoPoint.longitude
                        "Latitudine: ${latitudine ?: ""}, Longitudine: ${longitudine ?: ""}".also { coordinateEvento.text = it }
                    } else {
                        "Coordinate".also { coordinateEvento.text = it }
                    }
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

    private fun updateEvento() {
        val nomeEvento = editNomeEvento.text.toString().trim()
        val descrizioneEvento = editDescrizioneEvento.text.toString().trim()
        val tipoEvento = spinnerTipoEvento.selectedItem.toString()
        val data = etData.text.toString().trim()
        val oraInizio = etOraInizio.text.toString().trim()
        val oraFine = etOraFine.text.toString().trim()
        val prezzoEvento = editPrezzoEvento.text.toString().trim()

        if (nomeEvento.isEmpty() || descrizioneEvento.isEmpty() || data.isEmpty() || oraInizio.isEmpty() || oraFine.isEmpty() || prezzoEvento.isEmpty() || nomeLocale.isEmpty() || numeroTelefono.isEmpty() || latitudine == null || longitudine == null) {
            Toast.makeText(context, "Per favore, completa tutti i campi", Toast.LENGTH_SHORT).show()
            return
        }

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

        val geoPoint = GeoPoint(latitudine!!, longitudine!!)

        val updatedEvento = hashMapOf(
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
