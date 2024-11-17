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
import com.cuatrodivinas.seekandsolve.Datos.Data.Companion.auth
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
    private lateinit var rankingItems: MutableList<RankingItem>
    private lateinit var refImg: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        setupRankingList()
        findViewById<ImageView>(R.id.medalImageView).startAnimation(AnimationUtils.loadAnimation(this, R.anim.blink))
    }

    private fun setupRankingList() {
        val usersArray = readUserDataFromJson()
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
        }
        agregarRankingItemsDeMundoExterno(rankingItems)
        val rankingList = findViewById<RecyclerView>(R.id.rankingList)
        val sortedRankingItems = rankingItems.sortedByDescending { it.rewards }

        rankingList.layoutManager = LinearLayoutManager(this)
        rankingList.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_ranking, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val rankingItem = sortedRankingItems[position]
                val profileImageView: ImageView = holder.itemView.findViewById(R.id.profileImageView)
                val usernameTextView: TextView = holder.itemView.findViewById(R.id.usernameTextView)
                val rewardsTextView: TextView = holder.itemView.findViewById(R.id.scoreTextView)
                val verRecompensas: Button = holder.itemView.findViewById(R.id.btnRecompensas)

                refImg = storage.getReference(PATH_IMAGENES).child("${auth.currentUser!!.uid}.jpg")
                refImg.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(profileImageView.context)
                        .load(uri) // Carga la imagen desde la URL
                        .into(profileImageView)
                }.addOnFailureListener { exception ->
                    profileImageView.imageTintList = ContextCompat.getColorStateList(profileImageView.context, R.color.primaryColor)
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

    private fun agregarRankingItemsDeMundoExterno(rankingItems: MutableList<RankingItem>) {
        // Agregar rankingItems de un mundo externo
        rankingItems.add(RankingItem("valeria-sol", 10, "https://avatars.githubusercontent.com/u/155045120?v=4", "ejBDnjz1NKM8XcRBIWITcOUaNA"))
        rankingItems.add(RankingItem("fernando-leon", 5, "https://avatars.githubusercontent.com/u/155045119?v=4", "ejBDnjz1NKM8XcRBIWITcOUaNA"))
        rankingItems.add(RankingItem("lucia-perla", 3, "https://avatars.githubusercontent.com/u/155045118?v=4", "ejBDnjz1NKM8XcRBIWITcOUaNA"))
        rankingItems.add(RankingItem("pedro-salinas", 1, "https://avatars.githubusercontent.com/u/155045117?v=4", "ejBDnjz1NKM8XcRBIWITcOUaNA"))
    }

    fun isBase64(string: String): Boolean {
        return try {
            Base64.decode(string, Base64.DEFAULT)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun readUserDataFromJson(): JSONArray? {
        return try {
            val fileInputStream: FileInputStream = openFileInput("user_data.json")
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            JSONArray(String(bytes))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}