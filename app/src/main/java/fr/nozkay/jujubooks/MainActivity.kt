package fr.nozkay.jujubooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import fr.nozkay.jujubooks.R
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "BDDJujuBook"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getBooksData()

        val accueilbutton = findViewById<LinearLayout>(R.id.layout_home)
        val bibliobutton = findViewById<LinearLayout>(R.id.layout_biblio)
        val listebutton = findViewById<LinearLayout>(R.id.layout_liste)
        val statsbutton = findViewById<LinearLayout>(R.id.layout_stats)

        bibliobutton.setOnClickListener{
            val i = Intent(this, BiblioActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

    }

    private fun getBooksData() {
        db.collection("Bibliotheque")
            .get()
            .addOnSuccessListener { result ->
                val documents = result.documents

                // Récupérer le nombre total de livres
                val totalBooks = documents.size
                val totalBooksText = findViewById<TextView>(R.id.nblivrebiblio)
                totalBooksText.text = totalBooks.toString()
                Log.d(TAG, "Nombre total de livres dans la bibliothèque: $totalBooks")

                // Trier les documents par date et récupérer les 3 plus récents
                val sortedDocs = sortDocumentsByDate(documents).take(3)

                // Stocker les données des 3 livres récents
                val recentBooks = sortedDocs.map { doc ->
                    Book(
                        name = doc.getString("Nom") ?: "Inconnu",
                        format = doc.getString("Format") ?: "Non spécifié",
                        date = doc.getString("Date") ?: "Non spécifiée",
                        note = doc.getLong("Note")?.toInt() ?: 0
                    )
                }

                val nameBook1 = findViewById<TextView>(R.id.nomlivre1)
                val formatBook1 = findViewById<TextView>(R.id.formatlivre1)
                val dateBook1 = findViewById<TextView>(R.id.datelecture1)
                val noteBook1 = findViewById<TextView>(R.id.notelivre1)
                val nameBook2 = findViewById<TextView>(R.id.nomlivre2)
                val formatBook2 = findViewById<TextView>(R.id.formatlivre2)
                val dateBook2 = findViewById<TextView>(R.id.datelecture2)
                val noteBook2 = findViewById<TextView>(R.id.notelivre2)
                val nameBook3 = findViewById<TextView>(R.id.nomlivre3)
                val formatBook3 = findViewById<TextView>(R.id.formatlivre3)
                val dateBook3 = findViewById<TextView>(R.id.datelecture3)
                val noteBook3 = findViewById<TextView>(R.id.notelivre3)


                recentBooks.forEachIndexed { index, book ->
                    if(index == 0){
                        nameBook1.text = book.name
                        formatBook1.text = "Format: " + book.format
                        dateBook1.text = "Date de lecture: " + book.date.toString()
                        noteBook1.text = book.note.toString() + "/20"
                    }else if(index == 1){
                        nameBook2.text = book.name
                        formatBook2.text = "Format: " + book.format
                        dateBook2.text = "Date de lecture: " + book.date.toString()
                        noteBook2.text = book.note.toString() + "/20"
                    }else{
                        nameBook3.text = book.name
                        formatBook3.text = "Format: " + book.format
                        dateBook3.text = "Date de lecture: " + book.date.toString()
                        noteBook3.text = book.note.toString() + "/20"
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erreur de récupération des documents", exception)
            }
    }

    private fun sortDocumentsByDate(documents: List<DocumentSnapshot>): List<DocumentSnapshot> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return documents
            .mapNotNull { doc ->
                val dateStr = doc.getString("Date")
                try {
                    dateStr?.let { dateFormat.parse(it) }?.let { date -> doc to date }
                } catch (e: Exception) {
                    Log.e(TAG, "Format de date invalide: $dateStr")
                    null
                }
            }
            .sortedByDescending { it.second } // Tri décroissant (plus récent en premier)
            .map { it.first } // Récupération des documents triés
    }

    data class Book(
        val name: String,
        val format: String,
        val date: String,
        val note: Int
    )
}
