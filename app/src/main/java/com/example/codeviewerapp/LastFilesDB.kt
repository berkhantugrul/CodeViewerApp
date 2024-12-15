package com.example.codeviewerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LastFilesDB : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileRepository: FileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_files_db)

        recyclerView = findViewById(R.id.filesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fileRepository = FileRepository(this)

        // Dosyaları veritabanından çekip RecyclerView ile göster
        val fileList = fileRepository.getAllFiles()

        // Dosyaları RecyclerView'de gösterecek adapter
        val fileAdapter = FileAdapter(fileList)
        recyclerView.adapter = fileAdapter

        // Ana activity'e geçmek için bir buton ekliyoruz
        val backToMainButton: Button = findViewById(R.id.backToMainButton)
        backToMainButton.setOnClickListener {
            // Ana activity'yi başlatan intent
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // İsteğe bağlı olarak activity'yi kapatmak
            finish() // Bu çağrı ile SecondActivity'yi kapatıyoruz
        }
    }
}
