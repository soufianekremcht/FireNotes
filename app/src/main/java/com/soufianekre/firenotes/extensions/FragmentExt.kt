package com.soufianekre.firenotes.extensions

import androidx.fragment.app.Fragment
import com.soufianekre.firenotes.data.prefs.AppConfig

val Fragment.appConfig: AppConfig? get() = if (context != null) AppConfig(context!!) else null
