package de.zerotask.android.hltb.api.hltb

import android.util.Log
import de.zerotask.android.hltb.model.Game
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt

/**
 * Created by Sven on 17. Okt. 2017.
 */
class HLTBParser {

    // html text markers
    companion object {
        const val IDENTIFIER_MAIN_STORY: String = "Main Story"
        const val IDENTIFIER_COMPLETIONIST: String = "Completionist"
    }


    private val api by lazy {
        HLTBWebAPI.create()
    }

    fun search(query: String): ArrayList<Game> {
        // TODO("do within other thread and don't block ui")
        // retrieve the response
        val body = api.search(query)
                .subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread())
                .blockingFirst()

        return parse(body.string())
    }

    private fun parseTime(text: String): Double {
        if(text == "--") {
            return 0.0
        }

        if(text.indexOf('-') > -1) {
            return handleRange(text)
        }

        return getTime(text)
    }

    private fun handleRange(text: String): Double {
        val range = text.split('-')
        return (getTime(range[0]) + getTime(range[1])) / 2.0
    }

    private fun getTime(text: String): Double {
        val time = text.substring(0, text.indexOf(' '))

        if (time.indexOf('\u00bd') > -1) {
            return 0.5 + parseInt(time.substring(0, text.indexOf('\u00bd')))

        }
        return parseDouble(time)
    }

    private fun parse(content: String): ArrayList<Game> {
        val document = Jsoup.parse(content, "", Parser.xmlParser())
        val lists = document.select("li")

        val array = ArrayList<Game>()
        lists.forEach { li ->
            val gameTitleElem = li.select("a").first()

            // game information
            val title = gameTitleElem.attr("title")
            val hrefID = gameTitleElem.attr("href")
            val detailID = hrefID.substring(hrefID.indexOf("?id=") + 4)
            val imageUrl = "https://howlongtobeat.com/" + gameTitleElem.select("img").first().attr("src")

            var main = 0.0
            var complete = 0.0
            var steps = 0

            // try to parse the game time
            val timeElements = li.select(".search_list_details_block")[0].child(0)
            println(timeElements)
            for(i in timeElements.children().indices) {
                // skip if possible
                if(steps > 0) {
                    steps--
                    continue
                }

                val element = timeElements.child(i)
                Log.d("PARSER", element.toString())
                if(element != null) {
                    val type = element.text().trim()
                    Log.d("PARSER", "div content: " + type)

                    when(type) {
                        IDENTIFIER_MAIN_STORY -> {
                            Log.d("PARSER", "Parsing time for element " + type)
                            val time = parseTime(timeElements.child(i + 1).text().trim())
                            Log.d("PARSER", "Found time: " + time)
                            println("Time: " + time)
                            main = time
                        }

                        IDENTIFIER_COMPLETIONIST -> {
                            Log.d("PARSER", "Parsing time for element " + type)
                            val time = parseTime(timeElements.child(i + 1).text().trim())
                            Log.d("PARSER", "Found time: " + time)
                            println("Time: " + time)
                            complete = time
                        }
                    }

                    // skip next div since we already read the value
                    steps = 1
                }
            }

            val game = Game(detailID, title, imageUrl, main, complete)
            Log.i("LIST", game.toString())
            array.add(game)
        }

        return array
    }
}