package com.cajetan.youtubeplus.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.cajetan.youtubeplus.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_settings, rootKey)
    }
}
