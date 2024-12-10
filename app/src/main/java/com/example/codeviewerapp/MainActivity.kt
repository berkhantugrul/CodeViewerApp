package com.example.codeviewerapp

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    // Dosya seçici için ActivityResultLauncher
    private val getFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            readFile(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        if(savedInstanceState == null)
        {
            replaceFragment(HomeFragment())
            navigationView.setCheckedItem(R.id.home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> replaceFragment(HomeFragment())
            R.id.coding_scr -> replaceFragment(CodingFragment())
            R.id.open_file -> openFilePicker()
            R.id.last_files -> replaceFragment(LastFilesFragment())
            R.id.settings -> replaceFragment(SettingsFragment())
            R.id.about -> replaceFragment(AboutFragment())
            R.id.exit -> finish()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Dosya seçici çağırma
    private fun openFilePicker() {
        getFile.launch("*/*") // "*" tüm dosya türlerine izin verir
    }

    private fun replaceFragment(fragment: Fragment)
    {
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else
        {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // Seçilen dosyayı okuma
    private fun readFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = InputStreamReader(inputStream)
            val fileContent = reader.readText()
            reader.close()

            // Dosya içeriğini göster
            Toast.makeText(this, "Dosya içeriği: $fileContent", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Dosya okuma hatası", Toast.LENGTH_SHORT).show()
        }
    }
}