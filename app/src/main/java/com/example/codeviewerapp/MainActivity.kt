package com.example.codeviewerapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import java.io.InputStreamReader
import androidx.activity.result.ActivityResultLauncher


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    /*
    // Dosya seçici için ActivityResultLauncher
    private val getFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            readFile(uri)
        }
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        // Handle the result
    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment()) // SettingsFragment ilk ekleniyor
                .commit()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CodingFragment(), CodingFragment::class.java.simpleName)
                .commit() // CodingFragment ekleniyor
        }

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

        // ActivityResultLauncher'ı burada kaydet
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { openAndDisplayFile(it) }
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Çekmece kayarken yapılacak işlemler (isteğe bağlı)
                hideKeyboard()
            }

            override fun onDrawerOpened(drawerView: View) {
                // Çekmece açıldığında klavyeyi gizle
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val currentFocusView = currentFocus
                currentFocusView?.let {
                    inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                return
            }

            override fun onDrawerStateChanged(newState: Int) {
                return
            }
        })

    }

    /*
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
    }*/

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_file -> {
                // Dosya seçme işlemini başlat
                filePickerLauncher.launch("text/*")
            }
            // Diğer işlemler
            R.id.home -> replaceFragment(HomeFragment())
            R.id.coding_scr -> replaceFragment(CodingFragment())
            R.id.last_files -> replaceFragment(LastFilesFragment())
            R.id.settings -> replaceFragment(SettingsFragment())
            R.id.about -> replaceFragment(AboutFragment())
            R.id.exit -> finish()

        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /*
    // Dosya seçici çağırma
    private fun openFilePicker() {
        getFile.launch("**") // "*" tüm dosya türlerine izin verir
    }*/

    private fun replaceFragment(fragment: Fragment)
    {
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
    /*
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
    }*/

    /*
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
    }*/

    private fun openAndDisplayFile(uri: Uri) {
        try {
            // Dosyayı okuma
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val fileContent = reader.use { it.readText() }
            reader.close()

            // İçeriği bir TextView'e veya başka bir bileşene göster
            val fragment = supportFragmentManager.findFragmentByTag("CodingFragment") as? CodingFragment
            fragment?.displayFileContent(fileContent)
            Toast.makeText(this, "File opened: $fileContent", Toast.LENGTH_LONG).show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Toast.makeText(this, "File could not opened!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        currentFocusView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}