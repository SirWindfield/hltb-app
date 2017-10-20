package de.zerotask.android.hltb

import android.app.Application

/**
 * Created by Sven on 18. Okt. 2017.
 */
class HLTBApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        System.setProperty("org.slf4j.simplelogger.defaultlog", "DEBUG")
    }
}