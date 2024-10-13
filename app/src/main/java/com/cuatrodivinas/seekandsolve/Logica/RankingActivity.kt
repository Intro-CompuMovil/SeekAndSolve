package com.cuatrodivinas.seekandsolve.Logica

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cuatrodivinas.seekandsolve.R
import org.json.JSONObject
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
        val json = cargarJson()?.let { JSONObject(it) }
        val ranking = json?.getJSONArray("users")
        rankingItems = mutableListOf()
        if (ranking != null) {
            for (i in 0 until ranking.length()) {
                val jsonObject = ranking.getJSONObject(i)
                val username = jsonObject.getString("username")
                val rewards = jsonObject.getInt("recompensas")
                val profileImageUrl = jsonObject.getString("photoUrl")
                rankingItems.add(RankingItem(username, rewards, profileImageUrl))
            }
        }

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

                Glide.with(profileImageView.context)
                    .load(rankingItem.profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView)

                usernameTextView.text = rankingItem.username
                rewardsTextView.text = getString(R.string.rewards_text, rankingItem.rewards)
            }

            override fun getItemCount() = sortedRankingItems.size
        }
    }

    private fun cargarJson(): String? {
        val json: String?
        try {
            // TODO: Leer carrerasUsuarios.json y hacer un count de quienes tengan más recompensas
            val isStream: InputStream = assets.open("carrerasUsuarios.json")
            val size: Int = isStream.available()
            val buffer = ByteArray(size)
            isStream.read(buffer)
            isStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

}