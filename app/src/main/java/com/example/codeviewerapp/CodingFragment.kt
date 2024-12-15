package com.example.codeviewerapp

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
import android.widget.Button
import java.util.regex.Pattern
import com.amrdeveloper.codeview.CodeView
import com.example.codeviewerapp.databinding.FragmentCodingBinding

class CodingFragment : Fragment() {

    private var _binding: FragmentCodingBinding? = null
    private val binding get() = _binding!!
    private lateinit var codeView: CodeView
    //private lateinit var savebutton : Button

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
        _binding = FragmentCodingBinding.inflate(inflater, container, false)

        return binding.root
    }

    /*
    // Dosya içeriğini buraya yükle
    fun loadFile(uri: Uri?) {
        uri?.let {
            // Dosyayı okuma işlemi ve codeView'a yazdırma
            val content = readFileContent(it)
            code = content
        }
    }*/

    /*
    private fun readFileContent(uri: Uri): String {
        val contentResolver = context?.contentResolver
        val inputStream = contentResolver?.openInputStream(uri)
        val reader = inputStream?.bufferedReader()
        return reader?.use { it.readText() } ?: ""
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeView = view.findViewById(R.id.codeView)
        //savebutton = view.findViewById(R.id.saveButton)

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

        /*
        // Argümanları al
        val fileUri: Uri? = arguments?.getParcelable("file_uri")

        // Örneğin, dosyaya yazmayı burada da yapabilirsiniz
        savebutton.setOnClickListener {
            val updatedContent = binding.codeView.text.toString()
            fileUri?.let { uri ->
                if (writeTextToFile(uri, updatedContent)) {
                    Toast.makeText(requireContext(), "Dosya basariyla kaydedildi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Dosya kaydedilirken bir hata olustu", Toast.LENGTH_SHORT).show()
                }
            }
        }*/

        ////////////////////////////////////////////////////////////////////////////////////////////
        // TextWatcher ile kullanıcı girişini izleme
        var isTextWatcherActive = true // Recursive callback'ı önlemek için bayrak

        codeView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!isTextWatcherActive) return // Recursive tetiklemeyi önle

                try {
                    val cursorPosition = codeView.selectionStart // İmlecin pozisyonu
                    val text = codeView.text.toString() // Mevcut metin

                    // İmlecin solundaki metni bul
                    val beforeCursorText = text.substring(0, cursorPosition)
                    val lastWord = beforeCursorText.split("\\s+".toRegex()).lastOrNull() ?: return

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
        ////////////////////////////////////////////////////////////////////////////////////////////
        val pythonCode = """
            a = int(input())
            b = int(input())
            
            def add(a, b):
                for i in range(2):
                    total = a + (2 * b)
                return total
            
            while a > 0:
                print(add(a, b))
                a = a-1
                if a == 1:
                    break
            
        """.trimIndent()
        /*
        // Kod vurgulama (syntax highlighting) için renkler
        val keywords = listOf("def", "add", "int", "input", "return", "print")
        val colors = mapOf(
            "add" to Color.CYAN,
            "def" to Color.YELLOW,
            "print" to Color.YELLOW,
            "int" to Color.GREEN,
            "input" to Color.MAGENTA,
            "return" to Color.RED
        )*/

        val lightBlue = Color.rgb(65, 162, 241)
        val orange = Color.rgb(252, 160, 2)
        val darkGreen = Color.rgb(68, 160, 72)
        val turquoise = Color.rgb(39, 165, 153)
        val lightGreen = Color.rgb(76, 175, 80)
        val blue = Color.rgb(27, 117, 207)
        val lightRed = Color.rgb(237, 83, 80)

        // şunları da kategorilere göre birleştir ve C kalıplarını da ekle - BEKLESIN
        codeView.addSyntaxPattern(Pattern.compile("\\bdef\\b", Pattern.MULTILINE), lightRed)
        codeView.addSyntaxPattern(Pattern.compile("\\badd\\b", Pattern.MULTILINE), Color.CYAN)
        codeView.addSyntaxPattern(Pattern.compile("\\bint\\b", Pattern.MULTILINE), lightGreen)
        codeView.addSyntaxPattern(Pattern.compile("\\binput\\b", Pattern.MULTILINE), Color.GREEN)
        codeView.addSyntaxPattern(Pattern.compile("\\breturn\\b", Pattern.MULTILINE), blue)
        codeView.addSyntaxPattern(Pattern.compile("\\bprint\\b", Pattern.MULTILINE), turquoise)
        codeView.addSyntaxPattern(Pattern.compile("\\bfor\\b", Pattern.MULTILINE), Color.YELLOW)
        codeView.addSyntaxPattern(Pattern.compile("\\bwhile\\b", Pattern.MULTILINE), Color.YELLOW)
        codeView.addSyntaxPattern(Pattern.compile("\\bif\\b", Pattern.MULTILINE), Color.CYAN)
        codeView.addSyntaxPattern(Pattern.compile("\\belif\\b", Pattern.MULTILINE), Color.CYAN)
        codeView.addSyntaxPattern(Pattern.compile("\\belse\\b", Pattern.MULTILINE), Color.CYAN)
        codeView.addSyntaxPattern(Pattern.compile("\\brange\\b", Pattern.MULTILINE), lightRed)
        codeView.addSyntaxPattern(Pattern.compile("\\bin\\b", Pattern.MULTILINE), Color.YELLOW)
        codeView.addSyntaxPattern(Pattern.compile("\\bfloat\\b", Pattern.MULTILINE), Color.GREEN)
        codeView.addSyntaxPattern(Pattern.compile("\\bnot\\b", Pattern.MULTILINE), lightRed)
        codeView.addSyntaxPattern(Pattern.compile("\\blist\\b", Pattern.MULTILINE), Color.GREEN)
        codeView.addSyntaxPattern(Pattern.compile("\\bclass\\b", Pattern.MULTILINE), Color.MAGENTA)
        codeView.addSyntaxPattern(Pattern.compile("\\bbreak\\b", Pattern.MULTILINE), Color.GREEN)
        codeView.addSyntaxPattern(Pattern.compile("\\bcontinue\\b", Pattern.MULTILINE), Color.GREEN)
        codeView.addSyntaxPattern(Pattern.compile("\\bpass\\b", Pattern.MULTILINE), Color.GREEN)
        codeView.addSyntaxPattern(Pattern.compile("\\b\\d+(\\.\\d+)?\\b"), lightBlue)
        codeView.addSyntaxPattern(Pattern.compile("#.*"), darkGreen)
        codeView.addSyntaxPattern(Pattern.compile("\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\""), orange)

        //codeView.setText(pythonCode)
        //codeView.setText(code)

        // LINE NUMBERS
        codeView.setEnableLineNumber(true)
        codeView.setEnableHighlightCurrentLine(true)
        codeView.setHighlightCurrentLineColor(Color.rgb(85, 85, 85))
        codeView.setLineNumberTextColor(Color.WHITE)
        codeView.setLineNumberTextSize(45f)

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


    }


}