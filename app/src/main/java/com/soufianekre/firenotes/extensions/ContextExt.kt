package com.soufianekre.firenotes.extensions

import android.content.Context
import com.bumptech.glide.util.Util.isOnMainThread
import com.soufianekre.firenotes.data.db.NotesDatabase
import com.soufianekre.firenotes.data.prefs.AppConfig


fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

fun Context.notesDB() : NotesDatabase = NotesDatabase.getInstance(this)

val Context.appConfig : AppConfig
    get() = AppConfig(this)

