package de.zerotask.android.hltb.api

import de.zerotask.android.hltb.api.hltb.HLTBParser

/**
 * Created by Sven on 16. Okt. 2017.
 */
object API {

    val hltbAPI by lazy {
        HLTBParser()
    }
}