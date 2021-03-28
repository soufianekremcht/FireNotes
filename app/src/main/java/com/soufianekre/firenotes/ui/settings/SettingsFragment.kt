package com.soufianekre.firenotes.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.ui.base.BasePreferenceFragmentCompat

public class SettingsFragment : BasePreferenceFragmentCompat() ,Preference.OnPreferenceChangeListener
    ,Preference.OnPreferenceClickListener{


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_layout,rootKey)
        setupPreferences()
    }

    private fun setupPreferences() {

    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {

        return true

    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return true
    }


}