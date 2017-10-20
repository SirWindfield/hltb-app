package de.zerotask.android.hltb.api.hltb

import android.util.Log
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import de.zerotask.android.hltb.model.Game
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import mu.KLogging
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt

/**
 * A parser used to parse the html response from https://howlongtobeat.com.
 */
class HLTBParser {

    companion object {

        /**
         * The identifier used to search for the main story gameplay time.
         */
        const val IDENTIFIER_MAIN_STORY: String = "Main Story"

        /**
         * The identifier used to search for the completionist gameplay time.
         */
        const val IDENTIFIER_COMPLETIONIST: String = "Completionist"

        /*
         * The logger instance used to log debug information.
         */
        val logger = KLogging().logger
    }

    // created lazily, if the user doesn't need the api there is no need in creating it.
    private val api by lazy {
        HLTBWebAPI.create()
    }

    init {
        // make sure that we set the logger level to debug.
        // the underlying slf4j implementation used here is logback.
        (logger.underlyingLogger as Logger).level = Level.DEBUG
    }

    /**
     * Searches the howlongtobeat database for any game matching the search query.
     *
     * @param query The search query used for retrieving any matching game.
     * @return An observable stream pushing game instances.
     */
    fun search(query: String): Observable<Game> {
        // retrieve the response
        return api.search(query)                                     // ui thread
                .subscribeOn(Schedulers.computation())               // io thread
                .flatMapObservable { processResponse(it.string()) }  // io thread
    }

    /**
     * Processes the response by converting the response body into an observable.
     */
    private fun processResponse(response: String): Observable<Game> {
        return Observable.fromIterable(parse(response))
    }

    /**
     * Parses the given gameplay time.
     *
     * Time can be either a range of times or a static time quantity.
     */
    private fun parseTime(text: String): Double {
        if (text == "--") {
            return 0.0
        }

        if (text.indexOf('-') > -1) {
            return handleRange(text)
        }

        return getTime(text)
    }

    /**
     * Handles time range parsing.
     */
    private fun handleRange(text: String): Double {
        val range = text.split('-')
        return (getTime(range[0]) + getTime(range[1])) / 2.0
    }

    /**
     * Returns the time from a string representing the time.
     *
     * E.g.: 10, 10 1/4, 24, 32.4 etc...
     */
    private fun getTime(text: String): Double {
        val time = text.substring(0, text.indexOf(' '))

        if (time.indexOf('\u00bd') > -1) {
            return 0.5 + parseInt(time.substring(0, text.indexOf('\u00bd')))

        }
        return parseDouble(time)
    }

    /**
     * Parses the whole response body and returns an array list holding each found
     * game.
     *
     * This method will be wrapped within an Observable to prevent any thread blocking
     * and enable async calls to the API.
     */
    private fun parse(content: String): ArrayList<Game> {
        val doc = Jsoup.parse(content, "", Parser.xmlParser())
        val lists = doc.select("li")

        val array = ArrayList<Game>()
        lists.forEach { li ->
            val gameTitleElem = li.select("a").first()

            // only proceed if there are actually games returned from the api
            if (gameTitleElem != null) {

                // game information
                val title = gameTitleElem.attr("title")
                val hrefID = gameTitleElem.attr("href")
                val detailID = hrefID.substring(hrefID.indexOf("?id=") + 4)
                val imageUrl = "https://howlongtobeat.com/" + gameTitleElem.select("img").first().attr("src")

                logger.debug { "Processing game title $title" }

                var main = 0.0
                var complete = 0.0
                var steps = 0

                // try to parse the game time
                val timeElements = li.select(".search_list_details_block")[0].child(0)
                for (i in timeElements.children().indices) {

                    // skip if possible
                    // this small code is needed since kotlin doesn't allow one to
                    // break the current for-each iteration.
                    // TODO("maybe goto?")
                    if (steps > 0) {
                        steps--
                        continue
                    }

                    val element = timeElements.child(i)
                    //Log.d("PARSER", element.toString())
                    if (element != null) {
                        val type = element.text().trim()
                        //Log.d("PARSER", "div content: " + type)

                        when (type) {
                            IDENTIFIER_MAIN_STORY -> {
                                main = parseTime(timeElements.child(i + 1).text().trim())
                            }

                            IDENTIFIER_COMPLETIONIST -> {
                                complete = parseTime(timeElements.child(i + 1).text().trim())
                            }
                        }

                        // skip next div since we already read the value
                        steps = 1
                    }
                }

                val game = Game(detailID, title, imageUrl, main, complete)
                array.add(game)
            }
        }

        logger.debug { "Found a total of ${array.size} game titles matching $content" }
        return array
    }
}