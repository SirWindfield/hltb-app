package de.zerotask.android.hltb.model

/**
 * Created by Sven on 16. Okt. 2017.
 */
data class Game(val id: String, val name: String, val imageUrl: String,
                val gameplayMain: Double, val gameplayCompletionist: Double, val similarity: Double)