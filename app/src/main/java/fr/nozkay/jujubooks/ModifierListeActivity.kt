package fr.nozkay.jujubooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ModifierListeActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "BDDJujuBook"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modifier_liste)

        loadBooks()

        val accueilbutton = findViewById<LinearLayout>(R.id.layout_home)
        val bibliobutton = findViewById<LinearLayout>(R.id.layout_biblio)
        val confirmbutton = findViewById<TextView>(R.id.buttonConfirm)
        val listebutton = findViewById<LinearLayout>(R.id.layout_liste)
        val statsbutton = findViewById<LinearLayout>(R.id.layout_stats)

        confirmbutton.setOnClickListener{
            val i = Intent(this, ListeActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

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
    }

    private fun loadBooks() {
        db.collection("Liste")
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
        val bookView = inflater.inflate(R.layout.layout_base_suppr_liste, container, false)

        val nomLivre = bookView.findViewById<TextView>(R.id.nomlivre)
        val formatLivre = bookView.findViewById<TextView>(R.id.formatlivre)
        val dateLecture = bookView.findViewById<TextView>(R.id.datelecture)
        val supprButton = bookView.findViewById<TextView>(R.id.ButtonSuppr)
        val modifButton = bookView.findViewById<TextView>(R.id.ButtonModifNote)

        modifButton.setOnClickListener{
            val docId = doc.id
            val editText = android.widget.EditText(this)
            editText.hint = "Entrez le nouveau prix"

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Modifier le prix")
                .setMessage("Entrez le nouveau prix pour ce livre :")
                .setView(editText)
                .setPositiveButton("Valider") { _, _ ->
                    val newNote = editText.text.toString().trim()

                    if (newNote.isNotEmpty()) {
                        db.collection("Liste").document(docId)
                            .update("Prix", newNote)
                            .addOnSuccessListener {
                                Log.d(TAG, "Note mise à jour: $newNote")
                                modifButton.text = "Prix: $newNote" // Mise à jour dans l'UI
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Erreur lors de la mise à jour", e)
                            }
                    } else {
                        Log.w(TAG, "Champ vide, mise à jour annulée")
                    }
                }
                .setNegativeButton("Annuler", null)
                .create()

            alertDialog.show()
        }

        supprButton.setOnClickListener{
            val docId = doc.id
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet élément ?")
                .setPositiveButton("Oui") { _, _ ->
                    db.collection("Liste").document(docId)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "Livre supprimé: $docId")
                            container.removeView(bookView)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Erreur lors de la suppression", e)
                        }
                }.setNegativeButton("Annuler", null).create()

            alertDialog.show()

        }

        nomLivre.text = doc.getString("Nom") ?: "Inconnu"
        formatLivre.text = "Format: ${doc.getString("Format") ?: "Non spécifié"}"
        dateLecture.text = "Date de sortie: ${doc.getString("Date") ?: "Non spécifiée"}"

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