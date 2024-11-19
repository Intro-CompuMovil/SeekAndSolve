package com.cuatrodivinas.seekandsolve.Logica

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_IMAGENES
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.PATH_USERS
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.database
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.storage
import com.cuatrodivinas.seekandsolve.R
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


data class RankingItem(val username: String, val rewards: Int, val profileImageUrl: String, val uid: String)

class RankingActivity : AppCompatActivity() {
    private var rankingItems: MutableList<RankingItem> = mutableListOf()
    private lateinit var refImg: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        setupRankingList()
        findViewById<ImageView>(R.id.medalImageView).startAnimation(AnimationUtils.loadAnimation(this, R.anim.blink))
    }

    private fun setupRankingList() {
        val refUsers = database.getReference("$PATH_USERS/")
        refUsers.get().addOnSuccessListener { dataSnapshot ->
            // Obtener solo el "value" del DataSnapshot
            val valueMap = dataSnapshot.value as? Map<*, *>

            if (valueMap != null) {
                // Convertir el Map a un JSONObject
                val usersJson = JSONObject(valueMap)

                // Iterar sobre los usuarios
                for (key in usersJson.keys()) {
                    val userObject = usersJson.getJSONObject(key)
                    val username = userObject.optString("nombreUsuario", "Usuario desconocido")
                    val recompensas = userObject.optJSONObject("recompensas") ?: JSONObject()
                    val uid = key

                    rankingItems.add(RankingItem(username, recompensas.length(), "", uid))
                }
            } else {
                Toast.makeText(this, "No se encontraron usuarios.", Toast.LENGTH_SHORT).show()
            }

            // Actualizar el RecyclerView después de procesar los datos
            actualizarRecyclerView()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener los datos: ${it.message}", Toast.LENGTH_SHORT).show()
        }


        /*val usersArray = readUserDataFromJson()
        rankingItems = mutableListOf()
        for (i in 0 until usersArray!!.length()) {
            val user = usersArray.getJSONObject(i)
            //obtener uid
            //val uid = user.getString("uid")
            val uid = "ejBDnjz1NKM8XcRBIWITcOUaNA"
            val username = user.getString("username")
            val recomepensas = if (user.has("recompensas")) {
                user.getJSONArray("recompensas")
            } else {
                JSONArray()
            }
            val foto: String = if (user.has("photoUrl")) {
                user.getString("photoUrl")
            } else {
                ""
            }
            rankingItems.add(RankingItem(username, recomepensas.length(), foto, uid))
        }*/
    }

    private fun actualizarRecyclerView() {
        val rankingList = findViewById<RecyclerView>(R.id.rankingList)
        val sortedRankingItems = rankingItems.sortedByDescending { it.rewards }

        rankingList.layoutManager = LinearLayoutManager(this)
        rankingList.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_ranking, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val rankingItem = sortedRankingItems[position]
                val profileImageView: ImageView =
                    holder.itemView.findViewById(R.id.profileImageView)
                val usernameTextView: TextView = holder.itemView.findViewById(R.id.usernameTextView)
                val rewardsTextView: TextView = holder.itemView.findViewById(R.id.scoreTextView)
                val verRecompensas: Button = holder.itemView.findViewById(R.id.btnRecompensas)

                refImg = storage.getReference(PATH_IMAGENES).child("${rankingItem.uid}.jpg")
                refImg.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(profileImageView.context)
                        .load(uri) // Carga la imagen desde la URL
                        .into(profileImageView)
                }.addOnFailureListener { exception ->
                    profileImageView.imageTintList = ContextCompat.getColorStateList(
                        profileImageView.context,
                        R.color.primaryColor
                    )
                }

                usernameTextView.text = rankingItem.username
                rewardsTextView.text = getString(R.string.rewards_text, rankingItem.rewards)

                verRecompensas.setOnClickListener {
                    val intent = Intent(holder.itemView.context, RecompensasCarrera::class.java)
                    intent.putExtra("username", rankingItem.username) // Pasar datos al perfil
                    holder.itemView.context.startActivity(intent)
                }
            }

            override fun getItemCount() = sortedRankingItems.size
        }
    }
}