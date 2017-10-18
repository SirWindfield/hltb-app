package de.zerotask.android.hltb

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.zerotask.android.hltb.model.Game

/**
 * Created by Sven on 18. Okt. 2017.
 */
class GameAdapter(private val context: Context,
                  var games: ArrayList<Game>) : RecyclerView.Adapter<GameAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val game = games[position]

        holder?.title?.text = game.name
        holder?.mainStory?.text = game.gameplayMain.toString()
        holder?.completionist?.text = game.gameplayCompletionist.toString()

        // load image
        Picasso.with(context)
                .load(game.imageUrl)
                .into(holder?.coverImage)
    }

    override fun getItemCount(): Int {
        return games.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val card = LayoutInflater.from(parent?.context)
                .inflate(R.layout.card_view_game, parent, false)
        return ViewHolder(card)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.txt_title)
        val mainStory: TextView = view.findViewById(R.id.txt_hours_main_story)
        val mainPlusExtra: TextView = view.findViewById(R.id.txt_hours_main_extra)
        val completionist: TextView = view.findViewById(R.id.txt_hours_completionist)
        val allStyles: TextView = view.findViewById(R.id.txt_hours_all_styles)

        val coverImage: ImageView = view.findViewById(R.id.img_cover)
    }
}