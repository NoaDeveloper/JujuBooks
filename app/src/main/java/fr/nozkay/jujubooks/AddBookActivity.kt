package fr.nozkay.jujubooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AddBookActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "AjoutLivre"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        val accueilbutton = findViewById<LinearLayout>(R.id.layout_home)
        val bibliobutton = findViewById<LinearLayout>(R.id.layout_biblio)
        val listebutton = findViewById<LinearLayout>(R.id.layout_liste)
        val statsbutton = findViewById<LinearLayout>(R.id.layout_stats)

        accueilbutton.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        bibliobutton.setOnClickListener{
            val i = Intent(this, BiblioActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        val nameLivreAdd = findViewById<EditText>(R.id.nameLivreAdd)
        val formatLivreAdd = findViewById<EditText>(R.id.formatLivreAdd)
        val dateLivreAdd = findViewById<EditText>(R.id.dateLivreAdd)
        val noteLivreAdd = findViewById<EditText>(R.id.noteLivreAdd)
        val buttonAdd = findViewById<TextView>(R.id.buttonAdd)

        buttonAdd.setOnClickListener {
            val nomLivre = nameLivreAdd.text.toString().trim()
            val formatLivre = formatLivreAdd.text.toString().trim()
            val dateLecture = dateLivreAdd.text.toString().trim()
            val noteLivreStr = noteLivreAdd.text.toString().trim()

            // Vérification si aucun champ n'est vide
            if (nomLivre.isEmpty() || formatLivre.isEmpty() || dateLecture.isEmpty() || noteLivreStr.isEmpty()) {
                showToast("Tous les champs doivent être remplis.")
                return@setOnClickListener
            }

            // Vérification si la note est un nombre valide et inférieur à 20
            val noteLivre = noteLivreStr.toIntOrNull()
            if (noteLivre == null || noteLivre !in 0..20) {
                showToast("La note doit être un nombre entre 0 et 20.")
                return@setOnClickListener
            }

            // Vérification du format de la date de lecture
            val dateRegex = Regex("""^\d{2}/\d{2}/(\d{2}|\d{4})$""")
            val dateFinale = when {
                dateLecture == "0" -> "Non lue" // Si 0, on enregistre une valeur vide
                dateLecture.matches(dateRegex) -> dateLecture // Si format valide, on garde
                else -> {
                    showToast("Format de date invalide. Utilisez JJ/MM/AA ou JJ/MM/AAAA.")
                    return@setOnClickListener
                }
            }

            // Création du livre sous forme de HashMap
            val nouveauLivre = hashMapOf(
                "Nom" to nomLivre,
                "Format" to formatLivre,
                "Date" to dateFinale,
                "Note" to noteLivre
            )

            // Ajout à Firestore
            db.collection("Bibliotheque")
                .add(nouveauLivre)
                .addOnSuccessListener {
                    showToast("Livre ajouté avec succès !")
                    val i = Intent(this, BiblioActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erreur lors de l'ajout du livre", e)
                    showToast("Erreur lors de l'ajout du livre.")
                }
        }
    }

    // Fonction pour afficher un message toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}