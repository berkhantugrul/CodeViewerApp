package com.example.codeviewerapp

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.View
import androidx.core.content.ContextCompat

class MyInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard
    private var isShifted = false // Shift

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keyboard_layout)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        // Tuşların renklerini ayarlayın
        //updateKeyColors()

        return keyboardView
    }

    /*
    private fun updateKeyColors() {
        val keys = keyboard.keys

        // İlk iki satırdaki tuşların rengini değiştir
        keys.forEachIndexed { index, key ->
            // İlk iki satırdaki tuşların rengini değiştirelim
            when (index) {
                in 0..20 -> { // İlk 6 tuşun rengini değiştir
                    key.icon = ContextCompat.getDrawable(this, R.drawable.key_sym_background) // Özel icon
                }
                in 21..58 -> { // 7. ve 12. tuşlar
                    key.icon = ContextCompat.getDrawable(this, R.drawable.key_background) // Yeşil icon
                }
                in 59..63 -> { // Son 10 tuş
                    key.icon = ContextCompat.getDrawable(this, R.drawable.key_sym_background) // Koyu gri icon
                }
            }
        }
        keyboardView?.invalidateAllKeys() // Klavyeyi güncelle
    }*/

    // Tuşa basıldığında yapılacak işlemler
    override fun onPress(primaryCode: Int) {
        // Tuş basıldığında yapılacak işlemleri burada kontrol edebilirsiniz.
        // Burada sadece basit bir etkileşim efekti ekliyoruz
        keyboardView.invalidate()  // Görünümü günceller
    }

    // Tuş bırakıldığında yapılacak işlemler
    override fun onRelease(primaryCode: Int) {
        // Burada tuş bırakıldığında yapılacak işlemleri kontrol edebilirsiniz
        keyboardView.invalidate()  // Görünümü tekrar günceller
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val inputConnection = currentInputConnection

        val bracketPairs = mapOf(
            '(' to ')',
            '{' to '}',
            '[' to ']',
            '"' to '"',
            '\'' to '\'',
            '<' to '>'
        )

        when (primaryCode) {
            // Backspace tuşuna basıldığında
            -5 -> {
                // Silme işlemi sırasında parantez çiftlerini kontrol et
                val beforeCursor = inputConnection.getTextBeforeCursor(1, 0)
                val afterCursor = inputConnection.getTextAfterCursor(1, 0)

                if (afterCursor != null) {
                    if (beforeCursor != null) {
                        if (beforeCursor.isNotEmpty() && afterCursor.isNotEmpty()) {
                            val openBracket = beforeCursor?.last()
                            val closeBracket = afterCursor.first()

                            // Eğer parantez çifti varsa, ikisini birden sil
                            if (bracketPairs[openBracket] == closeBracket) {
                                inputConnection.deleteSurroundingText(1, 1)
                                return
                            }
                        }
                    }
                }

                // Normal silme işlemi
                inputConnection.deleteSurroundingText(1, 0)
            }

            // Tab tuşuna basıldığında
            9 -> {
                val spaces = "    " // 4 boşluk karakteri
                inputConnection.commitText(spaces, 1) // 4 boşluğu ekle
            }

            // Shift tuşuna basıldığında (Shift tuşunun kodu 15)
            15 -> {
                isShifted = !isShifted // Shift durumunu değiştir
                keyboard.isShifted = isShifted // Klavyenin görünümünü güncelle
                keyboardView.invalidateAllKeys() // Klavyeyi yeniden çiz

                // Shift durumu değiştikten sonra yeni bir karakter eklemiyoruz,
                // Shift'in etkisi yalnızca harf yazma işlemine etki eder.
            }

            // Diğer tuşlara basıldığında
            else -> {
                // Shift durumuna göre yazılacak karakter
                val char = primaryCode.toChar()
                val text = if (isShifted) char.uppercaseChar() else char.lowercaseChar()
                inputConnection.commitText(text.toString(), 1)

                /*
                // Eğer Shift tuşuna basıldıktan sonra sadece bir kez büyük harf isteniyorsa,
                // aşağıdaki satır ile Shift'i kapatabilirsiniz:
                isShifted = false
                keyboard.isShifted = false
                keyboardView.invalidateAllKeys()*/
            }
        }
    }

    private fun updateShiftState() {
        keyboard.isShifted = isShifted
        keyboardView.invalidateAllKeys() // Tüm tuşları yeniden çiz
    }


    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
