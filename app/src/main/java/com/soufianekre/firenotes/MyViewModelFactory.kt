package com.soufianekre.firenotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soufianekre.firenotes.ui.main.MainViewModel
import com.soufianekre.firenotes.ui.settings.SettingsViewModel

class MyViewModelFactory : ViewModelProvider.Factory{


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel() as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class " + modelClass.name)
        }
    }

}