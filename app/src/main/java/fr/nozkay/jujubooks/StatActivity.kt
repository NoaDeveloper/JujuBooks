package fr.nozkay.jujubooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil

class StatActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "StatsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stat) // Assurez-vous que le bon fichier XML est utilisé

        val accueilbutton = findViewById<LinearLayout>(R.id.layout_home)
        val bibliobutton = findViewById<LinearLayout>(R.id.layout_biblio)
        val listebutton = findViewById<LinearLayout>(R.id.layout_liste)
        val statsbutton = findViewById<LinearLayout>(R.id.layout_stats)

        accueilbutton.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        bibliobutton.setOnClickListener{
            val i = Intent(this, BiblioActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        listebutton.setOnClickListener{
            val i = Intent(this, ListeActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        statsbutton.setOnClickListener{
            val i = Intent(this, StatActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        val nbLivreBiblio = findViewById<TextView>(R.id.nblivrebiblio)
        val nbLivreListe = findViewById<TextView>(R.id.nblivreliste)
        val moyenneNote = findViewById<TextView>(R.id.moyenneNote)
        val prixHaut = findViewById<TextView>(R.id.prixHaut)
        val formatPref = findViewById<TextView>(R.id.formatPref)
        loadBooks()

        countDocuments("Bibliotheque") { count ->
            nbLivreBiblio.text = count.toString()
        }

        countDocuments("Liste") { count ->
            nbLivreListe.text = count.toString()
        }

        findMostFrequentFormat { format -> formatPref.text = format.toString() }

        calculateAverageNote { moyenne ->
            moyenneNote.text = moyenne.toString()
        }

        findHighestPrice { highestPrice ->
            prixHaut.text = String.format("%.2f€", highestPrice)
        }
    }

    private fun countDocuments(collectionName: String, callback: (Int) -> Unit) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val count = result.size()
                Log.d(TAG, "Nombre de livres dans $collectionName : $count")
                callback(count)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors du comptage des documents dans $collectionName", e)
                callback(0) // Valeur par défaut en cas d'échec
            }
    }

    private fun calculateAverageNote(callback: (Int) -> Unit) {
        db.collection("Bibliotheque")
            .get()
            .addOnSuccessListener { result ->
                val notes = result.mapNotNull { it.getDouble("Note") }.filter { it > 0 } // Exclut les notes égales à 0
                if (notes.isNotEmpty()) {
                    val moyenne = ceil(notes.average()).toInt() // Moyenne arrondie à l'entier supérieur
                    Log.d(TAG, "Moyenne des notes (sans les 0) : $moyenne")
                    callback(moyenne)
                } else {
                    callback(0) // Si aucune note valide trouvée
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors du calcul de la moyenne des notes", e)
                callback(0)
            }
    }

    private fun findHighestPrice(callback: (Double) -> Unit) {
        db.collection("Liste")
            .get()
            .addOnSuccessListener { result ->
                val prices = result.mapNotNull {
                    it.getString("Prix")?.replace("€", "")?.trim()?.toDoubleOrNull()
                }
                val highestPrice = prices.maxOrNull() ?: 0.0
                Log.d(TAG, "Prix le plus élevé : $highestPrice")
                callback(highestPrice)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors de la recherche du prix le plus élevé", e)
                callback(0.0)
            }
    }

    private fun findMostFrequentFormat(callback: (String) -> Unit) {
        db.collection("Bibliotheque")
            .get()
            .addOnSuccessListener { result ->
                val formatCounts = mutableMapOf<String, Int>()

                result.forEach { doc ->
                    val format = doc.getString("Format")?.lowercase()
                        ?.replace("[éèêë]".toRegex(), "e")
                        ?.replace("[àâä]".toRegex(), "a")
                        ?.replace("[îï]".toRegex(), "i")
                        ?.replace("[ôö]".toRegex(), "o")
                        ?.replace("[ûü]".toRegex(), "u")
                        ?.replace("[ç]".toRegex(), "c")
                        ?.trim()

                    if (!format.isNullOrEmpty()) {
                        formatCounts[format] = formatCounts.getOrDefault(format, 0) + 1
                    }
                }

                val mostFrequentFormat = formatCounts.maxByOrNull { it.value }?.key ?: "Inconnu"
                Log.d(TAG, "Format le plus fréquent : $mostFrequentFormat")
                callback(mostFrequentFormat)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors du calcul du format le plus fréquent", e)
                callback("Inconnu")
            }
    }

    private fun loadBooks() {
        db.collection("Bibliotheque")
            .get()
            .addOnSuccessListener { result ->
                val documents = result.documents

                val topBooks = documents
                    .mapNotNull { doc ->
                        val note = doc.getDouble("Note") ?: 0.0
                        if (note > 0) Pair(doc, note) else null
                    }
                    .sortedByDescending { it.second }
                    .take(2) // Prendre les deux meilleurs

                val container = findViewById<LinearLayout>(R.id.layoutLivres)
                container.removeAllViews()

                topBooks.forEachIndexed { index, (doc, _) ->
                    addBookToLayout(container, doc, index)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erreur de récupération des documents", exception)
            }
    }

    private fun addBookToLayout(container: LinearLayout, doc: DocumentSnapshot, index: Int) {
        val inflater = LayoutInflater.from(this)
        val bookView = inflater.inflate(R.layout.layout_base, container, false)

        val nomLivre = bookView.findViewById<TextView>(R.id.nomlivre)
        val formatLivre = bookView.findViewById<TextView>(R.id.formatlivre)
        val dateLecture = bookView.findViewById<TextView>(R.id.datelecture)
        val noteLivre = bookView.findViewById<TextView>(R.id.notelivre)

        nomLivre.text = doc.getString("Nom") ?: "Inconnu"
        formatLivre.text = "Format: ${doc.getString("Format") ?: "Non spécifié"}"
        dateLecture.text = "Date de lecture: ${doc.getString("Date") ?: "Non spécifiée"}"
        noteLivre.text = "${doc.getDouble("Note")?.toInt() ?: "?"}/20"

        // Ajouter une marge sauf pour le premier élément
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (index > 0) layoutParams.setMargins(0, 5, 0, 0)

        bookView.layoutParams = layoutParams
        container.addView(bookView)
    }
}