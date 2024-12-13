package com.example.codeviewerapp

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.View

class MyInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard
    private var isShifted = false // Shift
    //private var isCapsLock = false // Caps Lock

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.keyboard_layout)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        return keyboardView
    }

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
            '"' to '"'
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
                updateShiftState() // Shift durumu güncelleniyor

                // Shift durumu değiştikten sonra yeni bir karakter eklemiyoruz,
                // Shift'in etkisi yalnızca harf yazma işlemine etki eder.
            }

            // Diğer tuşlara basıldığında
            else -> {
                var character = primaryCode.toChar() // Tuş kodunu karaktere dönüştür

                // Eğer Shift aktifse, harfi büyük yapıyoruz
                if (isShifted) {
                    character = character.toUpperCase() // Büyük harfe dönüştür
                }

                // Karakteri ekle
                inputConnection.commitText(character.toString(), 1)

                // Yazma işlemi tamamlandıktan sonra Shift'i sıfırla
                isShifted = false
                updateShiftState() // Shift durumunu sıfırla
            }
        }
    }


    private fun updateShiftState() {
        // val keyboardView = currentInputView as KeyboardView
        keyboardView.isShifted = isShifted // Shift durumu güncelleniyor
    }

    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
