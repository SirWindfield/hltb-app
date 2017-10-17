package de.zerotask.android.hltb.api.hltb

import de.zerotask.android.hltb.model.Game
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

/**
 * Created by Sven on 17. Okt. 2017.
 */
class HLTBParser {

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

            array.add(Game(detailID, title, imageUrl, 0.0, 0.0, 0.0))
        }

        return array
    }
}