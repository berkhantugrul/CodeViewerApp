package com.example.codeviewerapp

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences ile tema durumunu kaydetme
        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", AppCompatActivity.MODE_PRIVATE)

        // Switch widget'ını al
        val themeSwitch = view.findViewById<Switch>(R.id.themeSwitch)

        // Mevcut tema durumu kontrol et
        val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false)
        themeSwitch.isChecked = isDarkMode

        // Switch metnini güncelle
        if (isDarkMode) {
            themeSwitch.text = "Change Theme (Dark)"
        } else {
            themeSwitch.text = "Change Theme (Light)"
        }

        // Switch durumu değiştiğinde tema değişimini yap
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Koyu moda geç
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPreferences.edit().putBoolean("is_dark_mode", true).apply() // Koyu mod kaydet
                themeSwitch.text = "Change Theme (Dark)"
            } else {
                // Açık moda geç
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences.edit().putBoolean("is_dark_mode", false).apply() // Açık mod kaydet
                themeSwitch.text = "Change Theme (Dark)"
            }
        }
    }
}