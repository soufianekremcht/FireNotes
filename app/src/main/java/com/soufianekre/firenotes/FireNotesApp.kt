package com.soufianekre.firenotes

import android.app.Application
import timber.log.Timber

public class FireNotesApp : Application(){
    override fun onCreate() {
        super.onCreate()
        instance = this;
        Timber.plant(Timber.DebugTree())
    }

    companion object{
        var instance : FireNotesApp? = null

        fun getInstance(): Application{
            return instance!!
        }
    }
}