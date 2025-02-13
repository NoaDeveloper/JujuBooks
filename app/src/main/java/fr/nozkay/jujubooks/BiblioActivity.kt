package fr.nozkay.jujubooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class BiblioActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "BDDJujuBook"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblio)

        loadBooks()

        val Bookadd = findViewById<TextView>(R.id.buttonAdd)
        val BookModif = findViewById<TextView>(R.id.buttonModifier)
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

        Bookadd.setOnClickListener{
            val i = Intent(this, AddBookActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        BookModif.setOnClickListener{
            val i = Intent(this, ModifierBiblioActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun loadBooks() {
        db.collection("Bibliotheque")
            .get()
            .addOnSuccessListener { result ->
                val documents = result.documents

                val totalBooks = documents.size
                Log.d(TAG, "Nombre total de livres: $totalBooks")

                val container = findViewById<LinearLayout>(R.id.containerLayout)
                container.removeAllViews() // Évite les doublons lors du rechargement

                documents.forEachIndexed { index, doc ->
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
        noteLivre.text = "${doc.getLong("Note")?.toInt() ?: 0}/20"

        // Ajouter une marge de 10dp sauf pour le premier élément
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        if (index > 0) { // Appliquer une marge de 10dp sauf pour le premier élément
            layoutParams.setMargins(0, 15, 0, 0)
        }

        bookView.layoutParams = layoutParams

        container.addView(bookView)
    }
}
