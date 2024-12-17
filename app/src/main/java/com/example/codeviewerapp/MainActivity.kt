package com.example.codeviewerapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
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
import com.amrdeveloper.codeview.CodeView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    //private lateinit var codeView: CodeView
    private var selectedFileUri: Uri? = null
    private lateinit var fileRepository: FileRepository
    private var selectedMimeType: String = "text/*" // Varsayılan MIME türü

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileRepository = FileRepository(this)


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

    // Spinner'ı bir AlertDialog içinde göster
    private fun showLanguagePickerDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val spinner = Spinner(this)

        // LinearLayout oluşturuluyor
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 27, 36, 27) // Padding (dp değerini pixel'e dönüştürmek için kullanılacak)
        }

        // Spinner'a diller eklenir
        val languages = arrayOf("Python", "C", "C++", "Text")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Spinner'ı layout'a ekleyin
        layout.addView(spinner)

        dialogBuilder.setTitle("Choose a type.")
        dialogBuilder.setView(layout)

        dialogBuilder.setPositiveButton("OK") { _, _ ->
            // Seçilen dile göre MIME türünü al
            selectedMimeType = getMimeTypeForLanguage(spinner.selectedItem.toString())
            openFilePicker() // Dosya seçici başlat
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()

        // Dialog boyutunu ayarlamak için burada window parametrelerini kullanıyoruz
        dialog.setOnShowListener {
            val window = dialog.window
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, // Genişlik
                600 // Yükseklik (pixel cinsinden örnek bir değer)
            )
        }

        dialog.show()
    }

    // Seçilen dile göre MIME türünü döner
    private fun getMimeTypeForLanguage(language: String): String {
        return when (language) {
            "Python" -> "text/x-python"   // Python dosyaları
            "C" -> "text/x-csrc"          // C dosyaları
            "C++" -> "text/x-c++src"      // C++ dosyaları
            "Text" -> "text/plain"        // TXT dosyaları
            else -> "text/*"              // Varsayılan metin türü
        }
    }

    // Dosya seçici başlatılır
    private fun openFilePicker() {
        openFileResult.launch(selectedMimeType)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_file -> {
                // Dosya seçme işlemini başlat
                showLanguagePickerDialog()
                replaceFragment(CodingFragment())
            }

            R.id.save_file -> {
                saveFilePicker()
            }

            // Diğer işlemler
            R.id.home -> replaceFragment(HomeFragment())
            R.id.coding_scr -> replaceFragment(CodingFragment())

            R.id.last_files -> {
                val intent = Intent(this, LastFilesDB::class.java)
                startActivity(intent)
            }

            R.id.settings -> replaceFragment(SettingsFragment())
            R.id.about -> replaceFragment(AboutFragment())
            R.id.exit -> finish()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun saveFilePicker() {

        val suggestedFileName = when (selectedMimeType) {
            "text/plain" -> "new_file.txt"
            "text/x-python" -> "new_file.py"
            "text/x-csrc" -> "new_file.c"
            "text/x-c++src" -> "new_file.cpp"
            else -> "new_file.txt"
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = selectedMimeType // Kaydedilecek MIME tipi
            putExtra(Intent.EXTRA_TITLE, suggestedFileName) // Varsayılan dosya adı
        }

        saveFileResult.launch(intent)
    }

    private val openFileResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Dosya URI'si ile işlemi yap
                selectedFileUri = uri
                readTextFileContent(uri)?.let { content ->
                    // İçeriği XML'e aktar
                    updateTextViewWithContent(content)
                }
                saveFileToDatabase(it)
                Toast.makeText(this, "File opened and saved on DB.", Toast.LENGTH_LONG).show()
            }
        }

    private val saveFileResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    writeToFile(uri)
                }
            } else {
                Toast.makeText(this, "File not saved", Toast.LENGTH_SHORT).show()
            }
        }

    private fun writeToFile(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()
                // CodeView içeriğini al ve yaz
                writer.write(findViewById<CodeView>(R.id.codeView).text.toString())  // write() ile dosyayı temizleyip yeni içeriği ekliyoruz
                writer.flush()
            }
            Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readTextFileContent(uri: Uri): String? {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bufferedReader = inputStream.bufferedReader().use { it.readText() }

                // Dosya içeriğini CodingFragment'e gönderiyoruz
                val fileName = uri.path?.substringAfterLast("/")
                val fileExtension = fileName?.substringAfterLast(".", "") ?: ""

                Log.i("File Ext", "File$fileExtension")
                // CodingFragment'e uzantıyı gönderiyoruz
                val bundle = Bundle().apply {
                    putString("fileExtension", fileExtension)
                    putString("fileContent", bufferedReader)
                }

                // Kod içeriğini ve uzantıyı CodingFragment'e gönder
                val codingFragment = CodingFragment().apply {
                    arguments = bundle
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, codingFragment) // CodingFragment'ı göster
                    .commit()
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun updateTextViewWithContent(content: String) {
        findViewById<CodeView>(R.id.codeView).setText(content)
        Toast.makeText(this, "File opened.", Toast.LENGTH_SHORT).show()
    }

    private fun replaceFragment(fragment: Fragment)
    {
        val transaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        currentFocusView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun saveFileToDatabase(uri: Uri) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)

            if (it.moveToFirst()) {
                val fileName = it.getString(nameIndex)
                val fileSize = it.getLong(sizeIndex).toInt()
                val filePath = uri.toString()
                val fileExtension = fileName.substringAfterLast(".")

                // Veritabanına kaydediyoruz
                fileRepository.insertFile(fileName, fileSize, filePath, fileExtension)
            }
        }
    }
}