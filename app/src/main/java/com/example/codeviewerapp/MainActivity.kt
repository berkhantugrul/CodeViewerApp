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
    //private var selectedSaveMimeType: String = "text/*" // Varsayılan MIME türü
    private lateinit var codingFragment: CodingFragment


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

        // ana sayfa acilir
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


    // NavigationView icerisinde
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_file -> {
                // Dosya seçme işlemini başlat
                showLanguagePickerDialog()
                replaceFragment(CodingFragment())

                // NavigationView'deki seçili öğeyi sıfırla veya Coding'e geçir
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.menu.findItem(R.id.open_file).isChecked = false
                navigationView.menu.findItem(R.id.coding_scr)?.isChecked = true
            }

            R.id.save_file -> {
                saveFilePicker()

                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.menu.findItem(R.id.save_file).isChecked = false
                navigationView.menu.findItem(R.id.coding_scr)?.isChecked = true
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


    // Dosya seçici başlatılır
    private fun openFilePicker() {
        openFileResult.launch(selectedMimeType)
    }

    private fun saveFilePicker() {
        val codingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? CodingFragment
        val selectedExtension = codingFragment?.getSelectedExtension() ?: "txt"

        // Eğer cihazdan açılan bir dosya varsa uzantıyı koruyun
        val suggestedFileName = if (selectedFileUri != null) {
            val originalFileName = selectedFileUri?.path?.substringAfterLast("/") ?: ""
            originalFileName
        } else {
            when (selectedExtension) {
                "py" -> "new_file.py"
                "c" -> "new_file.c"
                "cpp" -> "new_file.cpp"
                else -> "new_file.txt"
            }
        }

        val mimeType = when (selectedExtension) {
            "py" -> "text/x-python"
            "c" -> "text/x-csrc"
            "cpp" -> "text/x-c++src"
            else -> "text/plain"
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, suggestedFileName)
        }

        saveFileResult.launch(intent)
    }

    private val openFileResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Dosya adını ve uzantısını al
                val fileName = it.lastPathSegment?.substringAfterLast("/")
                val fileExtension = fileName?.substringAfterLast(".", "")

                // Spinner güncellemek için CodingFragment'e gönder
                val codingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? CodingFragment
                codingFragment?.updateSpinnerSelection(fileExtension)

                // İçeriği okuyup göstermek için mevcut işlemleri devam ettir
                readTextFileContent(uri)?.let { content ->
                    updateTextViewWithContent(content)
                }
                saveFileToDatabase(it)
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

                // Dosya adını ve uzantısını al
                val fileName = getFileName(uri)
                val fileExtension = fileName?.let { getFileExtension(it) } ?: ""

                // Uzantıya göre dil seçimi yap
                val selectedLanguage = when (fileExtension.lowercase()) {
                    "py" -> "Python"
                    "c" -> "C"
                    "cpp" -> "C++"
                    else -> "Text"
                }

                // CodingFragment'e dosya içeriği ve dil bilgisi gönder
                val codingFragment = CodingFragment().apply {
                    arguments = Bundle().apply {
                        putString("fileContent", bufferedReader)
                        putString("selectedLanguage", selectedLanguage)

                        if (fileName != null) {
                            Log.d("File Info", "File Name: $fileName, Extension: $fileExtension, Language: $selectedLanguage")
                        }
                    // Seçili dili gönderiyoruz
                    }
                }

                // Fragment'i değiştirme işlemi
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, codingFragment)
                    .commit()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error reading file!", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (fileName == null) {
            fileName = uri.path?.substringAfterLast('/')
        }
        return fileName
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
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