package com.soufianekre.firenotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soufianekre.firenotes.ui.main.MainViewModel

class MyViewModelFactory : ViewModelProvider.Factory{


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class " + modelClass.name)
        }
    }

}