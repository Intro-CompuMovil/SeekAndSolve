package com.cuatrodivinas.seekandsolve.Logica

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cuatrodivinas.seekandsolve.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


data class RankingItem(val username: String, val rewards: Int, val profileImageUrl: String)

class RankingActivity : AppCompatActivity() {
    private lateinit var rankingItems: MutableList<RankingItem>

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
            rankingItems.add(RankingItem(username, recomepensas.length(), foto))
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
                if (rankingItem.profileImageUrl.isNotEmpty()) {
                    if (rankingItem.profileImageUrl.startsWith("/")) {
                        // Cargar imagen desde el archivo
                        val file = File(rankingItem.profileImageUrl)
                        if (file.exists()) {
                            Glide.with(profileImageView.context)
                                .load(file)
                                .circleCrop() // Hacer la imagen circular
                                .into(profileImageView) // Establecer la imagen en el ImageView
                        } else {
                            // Caso: archivo no existe, cargar imagen por defecto
                            profileImageView.imageTintList = ContextCompat.getColorStateList(profileImageView.context, R.color.primaryColor)
                        }
                    } else if (rankingItem.profileImageUrl.startsWith("http://") || rankingItem.profileImageUrl.startsWith("https://")) {
                        // Cargar imagen desde una URL
                        Glide.with(profileImageView.context)
                            .load(rankingItem.profileImageUrl)
                            .circleCrop() // Hacer la imagen circular
                            .into(profileImageView) // Establecer la imagen en el ImageView
                    } else {
                        // Caso: cargar imagen por defecto si la fotoUrl no es válida
                        profileImageView.imageTintList = ContextCompat.getColorStateList(profileImageView.context, R.color.primaryColor)
                    }
                } else {
                    // Caso: fotoUrl está vacía, cargar imagen por defecto
                    profileImageView.imageTintList = ContextCompat.getColorStateList(profileImageView.context, R.color.primaryColor)
                }

                usernameTextView.text = rankingItem.username
                rewardsTextView.text = getString(R.string.rewards_text, rankingItem.rewards)
            }

            override fun getItemCount() = sortedRankingItems.size
        }

    }

    private fun agregarRankingItemsDeMundoExterno(rankingItems: MutableList<RankingItem>) {
        // Agregar rankingItems de un mundo externo
        rankingItems.add(RankingItem("valeria-sol", 10, "https://avatars.githubusercontent.com/u/155045120?v=4"))
        rankingItems.add(RankingItem("fernando-leon", 5, "https://avatars.githubusercontent.com/u/155045119?v=4"))
        rankingItems.add(RankingItem("lucia-perla", 3, "https://avatars.githubusercontent.com/u/155045118?v=4"))
        rankingItems.add(RankingItem("pedro-salinas", 1, "https://avatars.githubusercontent.com/u/155045117?v=4"))
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