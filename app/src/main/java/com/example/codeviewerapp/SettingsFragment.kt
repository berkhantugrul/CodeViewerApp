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
import com.example.codeviewerapp.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var showLineNumbersSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentSettingsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // SharedPreferences ile tema durumunu kaydetme
        sharedPreferences = requireActivity().getSharedPreferences("ThemePrefs", AppCompatActivity.MODE_PRIVATE)

        // Kullanıcı tercihlerinden 'Show Line Numbers' switch'inin durumunu ayarla
        val isShowLineNumbers = sharedPreferences.getBoolean("isShowLineNumbers", true)
        binding.lineCheckBox.isChecked = isShowLineNumbers

        // Show Line Numbers Switch listener
        binding.lineCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // Satır numaralarını göster ya da gizle
            sharedPreferences.edit().putBoolean("isShowLineNumbers", isChecked).apply()
            updateLineNumbersVisibility(isChecked)
        }

        // Başlangıçta satır numaralarını göster/gizle
        updateLineNumbersVisibility(isShowLineNumbers)

        return binding.root
    }



    @SuppressLint("SetTextI18n", "UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Switch widget'ını al
        val themeSwitch = view.findViewById<Switch>(R.id.themeSwitch)
        showLineNumbersSwitch = view.findViewById(R.id.lineCheckBox)

        // Cihazın mevcut temasını kontrol et
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        // Eğer cihaz karanlık modda ise, switch'i işaretle
        themeSwitch.isChecked = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES


        if (themeSwitch.isChecked)
            themeSwitch.text = "Change Theme (Dark)"
        else
            themeSwitch.text = "Change Theme (Light)"

        // Tema değiştirildiğinde kaydedelim
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Tema değişimi yapılır
            if (isChecked) {
                // Karanlık mod
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                themeSwitch.text = "Change Theme (Dark)"
            }
            else {
                // Aydınlık mod
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                themeSwitch.text = "Change Theme (Light)"
            }

            // Tema tercihini kaydedelim
            sharedPreferences.edit().putBoolean("isDarkMode", isChecked).apply()
        }
    }

    private fun updateLineNumbersVisibility(show: Boolean) {
        // CodingFragment'teki CodeView'e satır numaralarını göster/gizle
        val codingFragment = parentFragmentManager.findFragmentByTag(CodingFragment::class.java.simpleName) as? CodingFragment
        codingFragment?.setLineNumbers(show)
    }
}