package com.example.codeviewerapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FileDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Dosya bilgilerini saklayacak tabloyu oluşturuyoruz
        val CREATE_FILES_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_SIZE INTEGER,
                $COLUMN_PATH TEXT,
                $COLUMN_EXTENSION TEXT
            )
        """.trimIndent()

        db?.execSQL(CREATE_FILES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "fileManager.db"
        const val TABLE_NAME = "files"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_SIZE = "size"
        const val COLUMN_PATH = "path"
        const val COLUMN_EXTENSION = "extension"
    }
}
