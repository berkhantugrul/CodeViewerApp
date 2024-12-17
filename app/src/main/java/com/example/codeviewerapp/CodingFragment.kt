package com.example.codeviewerapp

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern
import com.amrdeveloper.codeview.CodeView
import com.example.codeviewerapp.databinding.FragmentCodingBinding

class CodingFragment : Fragment() {

    private lateinit var codeView: CodeView
    private lateinit var sharedPreferences: SharedPreferences
    private var fileExtension: String? = null
    private var fileContent : String? = null
    private var selectedLanguage: String? = null

    private val languages = listOf("Text", "Python", "C", "C++")
    private lateinit var spinnerFileType: Spinner

    //var code = ""

    private val pythonSnippets = mapOf(
        "for" to """ for i in range(n): 
    # Your code here
""",
        "if" to """ if condition: 
    # If block
else: 
    # Else block
""",
        "while" to """while condition:
    # Your code here
""")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Fragment'ı bağla
        val binding = FragmentCodingBinding.inflate(inflater, container, false)

        // CodeView'e zoom desteği ekliyoruz
        codeView = binding.codeView // CodeView'i binding'den alıyoruz

        // Arguments'ten dosya uzantısını al
        fileExtension = arguments?.getString("fileExtension")
        fileContent = arguments?.getString("fileContent")

        Log.d("CodingFragment", "File Extension: $fileExtension")

        // Dosya içeriğini CodeView'e set et
        // codeView.setText(fileContent)

        applyCode(codeView, fileContent, fileExtension)

        // SharedPreferences'i al
        sharedPreferences = requireActivity().getSharedPreferences("ThemePrefs", AppCompatActivity.MODE_PRIVATE)

        // "Show Line Numbers" tercihini al
        val isShowLineNumbers = sharedPreferences.getBoolean("isShowLineNumbers", true)

        // Satır numaralarını göster/gizle
        setLineNumbers(isShowLineNumbers)


        return binding.root
    }

    fun getSelectedExtension(): String {
        return when (spinnerFileType.selectedItem.toString()) {
            "Python" -> "py"
            "C" -> "c"
            "C++" -> "cpp"
            else -> "txt"
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeView = view.findViewById(R.id.codeView)
        spinnerFileType = view.findViewById(R.id.saveSpinner)

        codeView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_TAB && event.action == KeyEvent.ACTION_DOWN) {
                val cursorPosition = codeView.selectionStart
                codeView.text?.insert(cursorPosition, "    ") // 4 boşluk ekler
                codeView.setSelection(cursorPosition + 4) // İmleci 4 karakter ileri taşır
                true
            } else {
                false
            }

        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFileType.adapter = adapter

        // Eğer bir dil gönderildiyse spinner'ı güncelle
        arguments?.getString("selectedLanguage")?.let { selectedLanguage ->
            val selectedPosition = languages.indexOf(selectedLanguage)
            if (selectedPosition >= 0) {
                spinnerFileType.setSelection(selectedPosition)
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // TextWatcher ile kullanıcı girişini izleme
        var isTextWatcherActive = true // Recursive callback'ı önlemek için bayrak

        codeView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!isTextWatcherActive) return // Recursive tetiklemeyi önle

                if (selectedLanguage == "Python") {
                    try {
                        val cursorPosition = codeView.selectionStart // İmlecin pozisyonu
                        val text = codeView.text.toString() // Mevcut metin

                        // İmlecin solundaki metni bul
                        val beforeCursorText = text.substring(0, cursorPosition)
                        val lastWord =
                            beforeCursorText.split("\\s+".toRegex()).lastOrNull() ?: return

                        // Snippet'leri kontrol et
                        val snippet = when (lastWord) {
                            "if" -> """
                        if condition:
                            # your code
                        else:
                            # your code
                    """.trimIndent()

                            "for" -> """
                        for i in range(n):
                            # your code
                        
                    """.trimIndent()

                            "while" -> """
                        while condition:
                            # your code
                        
                    """.trimIndent()

                            else -> null
                        }

                        snippet?.let {
                            // Recursive tetiklemeyi önlemek için bayrağı devre dışı bırak
                            isTextWatcherActive = false

                            // Kelimeyi snippet ile değiştir
                            val startIndex = cursorPosition - lastWord.length
                            codeView.text.replace(startIndex, cursorPosition, it)
                            codeView.setSelection(startIndex + it.length) // İmleci yeni bloğa taşı

                            // Bayrağı tekrar etkinleştir
                            isTextWatcherActive = true
                        }
                    } catch (e: Exception) {
                        Log.e("CodeViewSnippet", "Error in snippet replacement", e)
                    }
                }

                else if (selectedLanguage == "C" || selectedLanguage == "C++")  {
                    try {
                        val cursorPosition = codeView.selectionStart // İmlecin pozisyonu
                        val text = codeView.text.toString() // Mevcut metin

                        // İmlecin solundaki metni bul
                        val beforeCursorText = text.substring(0, cursorPosition)
                        val lastWord =
                            beforeCursorText.split("\\s+".toRegex()).lastOrNull() ?: return

                        // Snippet'leri kontrol et
                        val snippet = when (lastWord) {
                            "if" -> """
                        if (condition) {
                                // your code
                            }
                            else {
                                // your code
                            }
                    """.trimIndent()

                            "for" -> """
                        for (int i = 0; i<n; i++) {
                                // your code
                            }
                    """.trimIndent()

                            "while" -> """
                        while (condition) {
                                // your code
                            }
                    """.trimIndent()

                            else -> null
                        }

                        snippet?.let {
                            // Recursive tetiklemeyi önlemek için bayrağı devre dışı bırak
                            isTextWatcherActive = false

                            // Kelimeyi snippet ile değiştir
                            val startIndex = cursorPosition - lastWord.length
                            codeView.text.replace(startIndex, cursorPosition, it)
                            codeView.setSelection(startIndex + it.length) // İmleci yeni bloğa taşı

                            // Bayrağı tekrar etkinleştir
                            isTextWatcherActive = true
                        }
                    } catch (e: Exception) {
                        Log.e("CodeViewSnippet", "Error in snippet replacement", e)
                    }
                }
            }
        })

        // Tab ile snippet tamamlama
        codeView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_TAB && event.action == KeyEvent.ACTION_DOWN) {
                val cursorPosition = codeView.selectionStart
                val text = codeView.text.toString()

                // Mevcut kelimeyi kontrol et
                val words = text.substring(0, cursorPosition).split(" ")
                val currentWord = words.lastOrNull() ?: ""

                // Snippet anahtar kelimesi
                if (pythonSnippets.containsKey(currentWord)) {
                    val snippet = pythonSnippets[currentWord]!!
                    val start = text.lastIndexOf(currentWord)
                    val end = start + currentWord.length

                    // Snippet'i yerleştir
                    codeView.text?.replace(start, end, snippet)
                    codeView.setSelection(start + snippet.length)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    fun updateSpinnerSelection(fileExtension: String?) {
        val languages = listOf("Text", "Python", "C", "C++")
        val spinnerSelection = when (fileExtension) {
            "txt" -> "Text"
            "py" -> "Python"
            "c" -> "C"
            "cpp" -> "C++"
            else -> "Text" // Varsayılan değer
        }
        val spinner = view?.findViewById<Spinner>(R.id.saveSpinner)
        spinner?.setSelection(languages.indexOf(spinnerSelection))
    }


    private fun loadFileContent(fileContent: String) {
        // CodeView'e içeriği ayarla
        codeView.setText(fileContent)

        // Satır sayısını hesapla
        val lineCount = fileContent.lines().size

        // Satır yüksekliğini al
        val paint = codeView.paint
        val lineHeight = paint.fontMetricsInt.run { bottom - top }

        // Toplam yüksekliği hesapla
        val totalHeight = lineCount * lineHeight

        // CodeView'in yüksekliğini ayarla
        val layoutParams = codeView.layoutParams as LinearLayout.LayoutParams

        if (lineCount > 38)
            layoutParams.height = totalHeight
        else
            layoutParams.height = 38 * lineHeight

        // Yüksekliği hesapla ve ayarla
        codeView.layoutParams = layoutParams
    }


    // Satır numaralarını göster/gizle
    fun setLineNumbers(show: Boolean) {
        if (show) {
            // LINE NUMBERS
            codeView.setEnableLineNumber(true)
            codeView.setEnableHighlightCurrentLine(true)
            codeView.setHighlightCurrentLineColor(Color.rgb(85, 85, 85))
            codeView.setLineNumberTextColor(Color.GRAY)
            codeView.setLineNumberTextSize(45f)

        }
        else {
            // Satır numaralarını gizle
            codeView.setEnableLineNumber(false)
        }
    }

    // DOSYA ACMA ISLEMLER ICIN
    private fun applyCode(codeView: CodeView, code: String?, ext:String?) {

        val lightBlue = Color.rgb(65, 162, 241)
        val lightOrange = Color.rgb(252,160,2)
        val darkGreen = Color.rgb(68, 160, 72)
        val turquoise = Color.rgb(0, 183, 255)
        val lightGreen = Color.rgb(0, 255, 115)
        val blue = Color.rgb(27, 138, 207)
        val lightRed = Color.rgb(237, 83, 80)
        val yellow = Color.rgb(242, 236, 68)

        if (ext == "txt")
        {
            codeView.addSyntaxPattern(Pattern.compile("[\\s\\S]*", Pattern.MULTILINE), Color.WHITE)
        }

        else {

            // Kontrol Yapıları ve Akış Kontrolü
            codeView.addSyntaxPattern(
                Pattern.compile("\\belse\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\belif\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bfinally\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            codeView.addSyntaxPattern(
                Pattern.compile("\\btry\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bexcept\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bbreak\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            codeView.addSyntaxPattern(
                Pattern.compile("\\bpass\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            codeView.addSyntaxPattern(
                Pattern.compile("\\byield\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            // Fonksiyon ve Değişken Tanımlamaları
            codeView.addSyntaxPattern(Pattern.compile("\\bdef\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\blambda\\b", Pattern.MULTILINE), blue)

            // İşlemciler ve Operatörler
            codeView.addSyntaxPattern(Pattern.compile("\\bor\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bis\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bin\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bstd\\b", Pattern.MULTILINE), lightGreen)

            // Modüller ve İçe Aktarım
            codeView.addSyntaxPattern(
                Pattern.compile("\\bimport\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bfrom\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            // Değerler ve Veri Tipleri
            codeView.addSyntaxPattern(Pattern.compile("\\bTrue\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bFalse\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bNone\\b", Pattern.MULTILINE), blue)

            // Özel Anahtar Kelimeler
            codeView.addSyntaxPattern(Pattern.compile("\\bglobal\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnonlocal\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(
                Pattern.compile("\\bassert\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bdel\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\braise\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            // Python Fonksiyonları (Built-in Fonksiyonlar - Sarı)
            codeView.addSyntaxPattern(Pattern.compile("\\babs\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\ball\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bany\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bbin\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bcallable\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bchr\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bcomplex\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bdict\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bdir\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bdivmod\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\benumerate\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\beval\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bexec\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bfilter\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bformat\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bfrozenset\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bgetattr\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bglobals\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bhasattr\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bhash\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bhelp\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bhex\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bid\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\binput\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bint\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(
                Pattern.compile("\\bisinstance\\b", Pattern.MULTILINE),
                yellow
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bissubclass\\b", Pattern.MULTILINE),
                yellow
            )
            codeView.addSyntaxPattern(Pattern.compile("\\biter\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\blen\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\blist\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\blocals\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bmap\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bmax\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(
                Pattern.compile("\\bmemoryview\\b", Pattern.MULTILINE),
                yellow
            )
            codeView.addSyntaxPattern(Pattern.compile("\\bmin\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bnext\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bobject\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\boct\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bopen\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bord\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bpow\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bprint\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bproperty\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\brange\\b", Pattern.MULTILINE), lightGreen)
            codeView.addSyntaxPattern(Pattern.compile("\\brepr\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\breversed\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bround\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bset\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bsetattr\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bslice\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bsorted\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(
                Pattern.compile("\\bstaticmethod\\b", Pattern.MULTILINE),
                yellow
            )
            codeView.addSyntaxPattern(Pattern.compile("\\bstr\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bsum\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bsuper\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\btuple\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\btype\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bvars\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(Pattern.compile("\\bzip\\b", Pattern.MULTILINE), yellow)
            codeView.addSyntaxPattern(
                Pattern.compile("\\b\\d+(\\.\\d+)?\\b", Pattern.MULTILINE),
                lightBlue
            )
            codeView.addSyntaxPattern(Pattern.compile("\\bendl\\b", Pattern.MULTILINE), turquoise)
            codeView.addSyntaxPattern(Pattern.compile("#.*", Pattern.MULTILINE), darkGreen)

            codeView.addSyntaxPattern(
                Pattern.compile(
                    "def\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\(",
                    Pattern.MULTILINE
                ), yellow
            ) // user-defined function names
            //codeView.addSyntaxPattern(Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*=", Pattern.MULTILINE), turquoise)


            val cBuiltInFunctions = listOf(
                "fclose", "fopen", "freopen", "fflush", "fseek", "ftell", "rewind",
                "fgetc", "fgetpos", "fgets", "fread", "getc", "getchar", "gets",
                "fputc", "fputs", "fprintf", "fwrite", "putc", "putchar", "puts",
                "printf", "scanf", "vprintf", "vsprintf", "sprintf", "main",
                "perror", "clearerr", "feof", "ferror",
                "setbuf", "setvbuf", "tempnam", "tmpfile", "tmpnam"
            )

            val regexPattern =
                cBuiltInFunctions.joinToString("|", prefix = "\\b(", postfix = ")\\b")
            codeView.addSyntaxPattern(Pattern.compile(regexPattern), yellow)

            codeView.addSyntaxPattern(Pattern.compile("#include", Pattern.MULTILINE),Color.MAGENTA)

            codeView.addSyntaxPattern(
                Pattern.compile("\\bswitch\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bcase\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(Pattern.compile("\\bdo\\b", Pattern.MULTILINE), Color.MAGENTA)

            codeView.addSyntaxPattern(
                Pattern.compile("\\bgoto\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            codeView.addSyntaxPattern(Pattern.compile("\\bdouble\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bchar\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\blong\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bstatic\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\binline\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bextern\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bshort\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bunsigned\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bstruct\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\btypedef\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bregister\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\brestrict\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bsizeof\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bvolatile\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bunion\\b", Pattern.MULTILINE), blue)

            codeView.addSyntaxPattern(Pattern.compile("\\b\\d+\\b", Pattern.MULTILINE), lightBlue)

            val cppIOPattern = Pattern.compile(
                "\\b(main|cin|cout|cerr|clog|getline|printf|fprintf|sprintf|vprintf|vsprintf|scanf|fscanf|sscanf|snprintf|vsnprintf|wcin|wcout|wcerr|wclog|wscanf|fwcin|fwcout|fwcerr|fwclog)\\b",
                Pattern.MULTILINE
            )
            codeView.addSyntaxPattern(cppIOPattern, yellow)

            codeView.addSyntaxPattern(Pattern.compile("\\balignas\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\balignof\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\band\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\band_eq\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bauto\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bbitand\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bbitor\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bbool\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bchar16_t\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bchar32_t\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bclass\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bcompl\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bconst\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bconstexpr\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bconst_cast\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bdecltype\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bdelete\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(
                Pattern.compile("\\bdynamic_cast\\b", Pattern.MULTILINE),
                blue
            )
            codeView.addSyntaxPattern(Pattern.compile("\\bendl\\b", Pattern.MULTILINE), lightRed)
            codeView.addSyntaxPattern(Pattern.compile("\\benum\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bexplicit\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bexport\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bextern\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bfinal\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bfloat\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bfriend\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\binline\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bmutuable\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnamespace\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnew\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnoexcept\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnot_eq\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnullptr\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\boperator\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bprivate\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bprotected\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bpublic\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bregister\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(
                Pattern.compile("\\breinterpret_cast\\b", Pattern.MULTILINE),
                blue
            )

            codeView.addSyntaxPattern(Pattern.compile("\\bsigned\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnamespace\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnew\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnoexcept\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnot\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnot_eq\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bnullptr\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\boperator\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bprivate\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bstatic\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(
                Pattern.compile("\\bstatic_assert\\b", Pattern.MULTILINE),
                blue
            )
            codeView.addSyntaxPattern(Pattern.compile("\\bstatic_cast\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bstruct\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\btemplate\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bthis\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(
                Pattern.compile("\\bthread_local\\b", Pattern.MULTILINE),
                blue
            )
            codeView.addSyntaxPattern(Pattern.compile("\\btypedef\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\btypeid\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\btypename\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bunion\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bunsigned\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\busing\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bvirtual\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bvolatile\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bvoid\\b", Pattern.MULTILINE), blue)
            codeView.addSyntaxPattern(Pattern.compile("\\bwchar_t\\b", Pattern.MULTILINE), blue)

            codeView.addSyntaxPattern(
                Pattern.compile("\\bcontinue\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bcatch\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bdefault\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\breturn\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            codeView.addSyntaxPattern(Pattern.compile("\\bif\\b", Pattern.MULTILINE), Color.MAGENTA)

            codeView.addSyntaxPattern(
                Pattern.compile("\\bfor\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bgoto\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bthrow\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )
            codeView.addSyntaxPattern(
                Pattern.compile("\\bwhile\\b", Pattern.MULTILINE),
                Color.MAGENTA
            )

            codeView.addSyntaxPattern(
                Pattern.compile("(//.*$|/\\*.*?\\*/)", Pattern.MULTILINE),
                darkGreen
            )
            codeView.addSyntaxPattern(Pattern.compile("\\b\\d+\\n\b", Pattern.MULTILINE), lightBlue)
            codeView.addSyntaxPattern(
                Pattern.compile("\"[^\"]*\"|\'[^\']*\'", Pattern.MULTILINE),
                lightOrange
            )

            codeView.addSyntaxPattern(
                Pattern.compile("\\b<stdio.h>\\b", Pattern.MULTILINE),
                lightRed
            ) //<stdio.h>

            codeView.addSyntaxPattern(
                Pattern.compile("\\b<iostream>\\b", Pattern.MULTILINE),
                lightRed
            ) //<stdio.h>
            //codeView.addSyntaxPattern(Pattern.compile("using\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*", Pattern.MULTILINE), orange) // user-defined function names
        }
        //codeView.setText(pythonCode)
        //codeView.setText(code)


        // PAIR COMPLETE
        codeView.enablePairComplete(true)
        codeView.enablePairCompleteCenterCursor(true)
        codeView.addPairCompleteItem('[',']')
        codeView.addPairCompleteItem('"','"')
        codeView.addPairCompleteItem('(',')')
        codeView.addPairCompleteItem('{','}')
        codeView.addPairCompleteItem('\'', '\'')
        codeView.addPairCompleteItem('<','>')

        // TAB INDENTATION
        val chars = mutableSetOf(':')
        codeView.setEnableAutoIndentation(true)
        codeView.setIndentationStarts(chars)
        codeView.setTabLength(4)

        codeView.setBackgroundColor(Color.DKGRAY)
        codeView.setTextColor(Color.WHITE)

        //codeView.setText(code)

        // CodeView'e içeriği yükle ve yüksekliği ayarla
        if (code != null) {
            loadFileContent(code)
        }
    }

}