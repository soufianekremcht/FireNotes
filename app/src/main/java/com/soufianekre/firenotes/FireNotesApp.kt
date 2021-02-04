package com.soufianekre.firenotes

import android.app.Application

public class FireNotesApp : Application(){
    override fun onCreate() {
        super.onCreate()
        instance = this;
    }

    companion object{
        var instance : FireNotesApp? = null

        fun getInstance(): Application{
            return instance!!
        }
    }
}