package com.example.codeviewerapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class FileRepository(context: Context) {

    private val dbHelper = FileDatabaseHelper(context)

    // Veritabanına dosya ekleme fonksiyonu
    fun insertFile(fileName: String, fileSize: Int, filePath: String, fileExtension: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(FileDatabaseHelper.COLUMN_NAME, fileName)
            put(FileDatabaseHelper.COLUMN_SIZE, fileSize)
            put(FileDatabaseHelper.COLUMN_PATH, filePath)
            put(FileDatabaseHelper.COLUMN_EXTENSION, fileExtension)
        }

        db.insert(FileDatabaseHelper.TABLE_NAME, null, values)
        db.close()
    }

    // Veritabanından dosya verilerini okuma fonksiyonu
    fun getAllFiles(): List<FileEntity> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(FileDatabaseHelper.TABLE_NAME, null, null, null, null, null, null)

        val files = mutableListOf<FileEntity>()

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(FileDatabaseHelper.COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(FileDatabaseHelper.COLUMN_NAME))
                val size = getInt(getColumnIndexOrThrow(FileDatabaseHelper.COLUMN_SIZE))
                val path = getString(getColumnIndexOrThrow(FileDatabaseHelper.COLUMN_PATH))
                val extension = getString(getColumnIndexOrThrow(FileDatabaseHelper.COLUMN_EXTENSION))

                files.add(FileEntity(id, name, size, path, extension))
            }
        }

        cursor.close()
        db.close()
        return files
    }
}
