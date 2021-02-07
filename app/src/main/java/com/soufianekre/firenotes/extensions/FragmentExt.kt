package com.soufianekre.firenotes.extensions

import androidx.fragment.app.Fragment
import com.soufianekre.firenotes.data.prefs.Config

val Fragment.config: Config? get() = if (context != null) Config(context!!) else null
